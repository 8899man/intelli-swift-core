package com.fr.swift.repository.impl;

import com.fr.swift.file.conf.SwiftFileSystemConfig;
import com.fr.swift.file.exception.SwiftFileException;
import com.fr.swift.file.system.SwiftFileSystem;
import com.fr.swift.repository.AbstractRepository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * @author yee
 * @date 2018/5/28
 */
public class SwiftRepositoryImpl extends AbstractRepository {
    public SwiftRepositoryImpl(SwiftFileSystemConfig configuration) {
        super(configuration);
    }

    @Override
    public URI copyFromRemote(URI remote, URI local) throws IOException {
        SwiftFileSystem from = createFileSystem(remote);
        File directory = new File(local.getPath()).getParentFile();
        if (!directory.exists()) {
            directory.mkdirs();
        }
        if (from.isExists()) {
            SwiftFileSystem target = from.read();
            if (!target.isDirectory()) {
                InputStream inputStream = null;
                FileOutputStream fileOutputStream = null;
                try {
                    inputStream = target.toStream();
                    fileOutputStream = new FileOutputStream(new File(local.getPath()));
                    byte[] bytes = new byte[1024];
                    int len = 0;
                    while ((len = inputStream.read(bytes, 0, bytes.length)) != -1) {
                        fileOutputStream.write(bytes, 0, len);
                    }
                    fileOutputStream.flush();
                } catch (Exception e) {
                    throw new SwiftFileException(e);
                } finally {
                    if (null != inputStream) {
                        inputStream.close();
                    }
                    if (null != fileOutputStream) {
                        fileOutputStream.close();
                    }
                }
            } else {
                SwiftFileSystem[] systems = from.listFiles();
                for (SwiftFileSystem fileSystem : systems) {
                    copyFromRemote(fileSystem.getResourceURI(), resolve(local, fileSystem.getResourceName()));
                }
            }
            return local;
        }
        throw new SwiftFileException(String.format("Remote resource '%s' is not exists!", remote.getPath()));
    }

    @Override
    public boolean copyToRemote(URI local, URI remote) throws IOException {
        File file = new File(local.getPath());
        if (file.isDirectory()) {
            SwiftFileSystem fileSystem = createFileSystem(remote);
            if (fileSystem.isExists()) {
                fileSystem.remove();
            }
            fileSystem.mkdirs();
            File[] files = file.listFiles();
            for (File f : files) {
                copyToRemote(f.toURI(), resolve(remote, f.getName()));
            }
        } else {
            FileInputStream inputStream = null;
            try {
                inputStream = new FileInputStream(file);
                SwiftFileSystem to = createFileSystem(remote);
                if (to.isExists()) {
                    to.remove();
                }
                to.write(inputStream);
            } catch (FileNotFoundException e) {
                throw new SwiftFileException(e);
            } finally {
                if (null != inputStream) {
                    inputStream.close();
                }
            }
        }
        return true;
    }

    private URI resolve(URI uri, String resolve) {
        String path = uri.getPath();
        if (path.endsWith("/")) {
            return uri.resolve(resolve);
        }
        return URI.create(path + "/").resolve(resolve);
    }
}

package com.finebi.cube.data.disk.reader;

import com.finebi.cube.common.log.BILoggerFactory;
import com.finebi.cube.data.input.ICubeByteArrayReader;
import com.finebi.cube.data.input.primitive.ICubeByteReader;
import com.finebi.cube.data.input.primitive.ICubeIntegerReader;
import com.finebi.cube.data.input.primitive.ICubeLongReader;
import com.finebi.cube.exception.BIResourceInvalidException;
import com.fr.bi.common.inter.Release;
import com.fr.bi.stable.constant.CubeConstant;
import com.fr.bi.stable.utils.program.BIStringUtils;

public class BIByteArrayNIOReader implements ICubeByteArrayReader, Release {

    private ICubeLongReader positionReader;

    private ICubeIntegerReader lengthReader;

    private ICubeByteReader contentReader;

    public BIByteArrayNIOReader(ICubeLongReader positionReader, ICubeIntegerReader lengthReader, ICubeByteReader contentReader) {
        this.positionReader = positionReader;
        this.lengthReader = lengthReader;
        this.contentReader = contentReader;
    }


    @Override
    public byte[] getSpecificValue(final int row) throws BIResourceInvalidException {
        long start = 0;
        int size = 0;
        try {
            start = positionReader.getSpecificValue(row);
            size = lengthReader.getSpecificValue(row);
        } catch (Exception e) {
            BILoggerFactory.getLogger().info(BIStringUtils.append(
                    e.getMessage(),
                    "\n" + "retry again!"
            ));
            releaseBuffers();
            start = positionReader.getSpecificValue(row);
            size = lengthReader.getSpecificValue(row);
        }
        if (size == 0) {
            return new byte[]{};
        }
        byte[] b = new byte[size];
        for (int i = 0; i < size; i++) {
            b[i] = contentReader.getSpecificValue(start + i);
        }
        return isNull(b) ? null : b;
    }

    private void releaseBuffers() {
        positionReader.releaseBuffer();
        lengthReader.releaseBuffer();
    }

    private boolean isNull(byte[] result) {
        if (result.length == CubeConstant.NULLBYTES.length) {
            for (int i = 0; i < result.length; i++) {
                if (result[i] != CubeConstant.NULLBYTES[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public long getLastPosition(long row) {
        if (row == 0) {
            return 0;
        }
        long start = 0;
        int size = 0;
        try {
            start = positionReader.getSpecificValue(row - 1);
        } catch (BIResourceInvalidException e) {
            BILoggerFactory.getLogger().error(e.getMessage(), e);
        }
        try {
            size = lengthReader.getSpecificValue(row - 1);
        } catch (BIResourceInvalidException e) {
            BILoggerFactory.getLogger().error(e.getMessage(), e);

        }
        return start + size;
    }

    @Override
    public void clear() {
        positionReader.releaseHandler();
        lengthReader.releaseHandler();
        contentReader.releaseHandler();
    }

    @Override
    public boolean canRead() {
        return contentReader.canReader();
    }

    @Override
    public void forceRelease() {
        positionReader.forceRelease();
        lengthReader.forceRelease();
        contentReader.forceRelease();
    }

    @Override
    public boolean isForceReleased() {
        return positionReader.isForceReleased() ||
                lengthReader.isForceReleased() ||
                contentReader.isForceReleased();
    }

    @Override
    public byte getFirstByte(int row) throws BIResourceInvalidException {
        long start;
        try {
            start = positionReader.getSpecificValue(row);
        } catch (Exception e) {
            BILoggerFactory.getLogger().info(BIStringUtils.append(
                    e.getMessage(),
                    "\n" + "retry again!"
            ));
            releaseBuffers();
            start = positionReader.getSpecificValue(row);
        }
        return contentReader.getSpecificValue(start);
    }
}
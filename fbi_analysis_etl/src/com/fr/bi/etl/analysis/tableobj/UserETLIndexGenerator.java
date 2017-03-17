/**
 * 
 */
package com.fr.bi.etl.analysis.tableobj;

import com.fr.bi.cal.generate.index.IndexGenerator;
import com.fr.bi.cal.stable.cube.file.TableCubeFile;
import com.fr.bi.etl.analysis.data.UserCubeTableSource;
import com.fr.bi.util.BIConfigurePathUtils;

/**
 * @author Daniel
 *
 */
public class UserETLIndexGenerator extends IndexGenerator {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = -3109798392790594046L;

	/**
	 * @param source
	 * @param userId
	 * @param version
	 */
	public UserETLIndexGenerator(UserCubeTableSource source, int version, String path) {
        super(source, path, source.getUserId(), version);
    }
	
    @Override
	protected void createTableCube() {
        cube = new TableCubeFile(BIConfigurePathUtils.createUserETLTablePath(source.fetchObjectCore().getID().getIdentityValue(), pathSuffix));
    }

}
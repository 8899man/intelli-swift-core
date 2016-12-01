/**
 *
 */
package com.finebi.cube.conf;

import com.finebi.cube.conf.pack.data.*;
import com.finebi.cube.conf.pack.group.IBusinessGroupGetterService;
import com.finebi.cube.conf.table.BusinessTable;
import com.fr.bi.conf.data.pack.exception.BIGroupAbsentException;
import com.fr.bi.conf.data.pack.exception.BIGroupDuplicateException;
import com.fr.bi.conf.data.pack.exception.BIPackageAbsentException;
import com.fr.bi.conf.data.pack.exception.BIPackageDuplicateException;
import com.fr.bi.stable.data.BITableID;
import com.fr.bi.stable.data.source.CubeTableSource;
import com.fr.bi.stable.exception.BITableAbsentException;
import com.fr.json.JSONException;
import com.fr.json.JSONObject;

import java.util.Locale;
import java.util.Set;

/**
 * 系统的业务包管理接口
 *
 * @author Connery
 */
public interface BISystemPackageConfigurationProvider {

    String XML_TAG = "BIBusiPackManager";

    /**
     * 完成生成cube
     *
     * @param userId 用户id
     */
    void finishGenerateCubes(long userId);

    /**
     * 完成生成cube
     *
     * @param userId用户id
     * @param absentTables 本次生成后仍然缺少的表
     */
    void finishGenerateCubes(long userId, Set<CubeTableSource> absentTables);

    /**
     * 获取当前最新版本的业务包用于数据更新
     *
     * @return
     */
    Set<IBusinessPackageGetterService> getAllPackages(long userId);


    /**
     * 当前用户的业务包是否有变化
     *
     * @param userId 用户ID
     * @return 是否变化
     */
    boolean isPackageDataChanged(long userId);

    /**
     * 更新
     */
    void envChanged();

    /**
     * 根据ID获得相应的业务包
     *
     * @param userId    用户ID
     * @param packageID 业务包ID
     * @return 业务包
     * @throws BIPackageAbsentException 业务包不存在
     */
    IBusinessPackageGetterService getPackage(long userId, BIPackageID packageID) throws BIPackageAbsentException;

    /**
     * 重命名业务包
     *
     * @param userId      用户ID
     * @param packageID   业务包ID
     * @param packageName 业务包名字
     * @throws BIPackageAbsentException    指定的业务包不存在
     * @throws BIPackageDuplicateException 业务包重复
     */
    void renamePackage(long userId, BIPackageID packageID, BIPackageName packageName) throws BIPackageAbsentException, BIPackageDuplicateException;

    /**
     * 重命名分组
     *
     * @param userId  用户ID
     * @param oldName 老的分组名字
     * @param newName 新的分组名字
     * @throws BIGroupAbsentException    老名字分组缺少抛错
     * @throws BIGroupDuplicateException 新名字的分组存在抛错
     */
    void renameGroup(long userId, BIGroupTagName oldName, BIGroupTagName newName) throws BIGroupAbsentException, BIGroupDuplicateException;


    /**
     * 添加一个业务包
     *
     * @param userId            用户ID
     * @param biBusinessPackage 业务包
     * @throws BIPackageDuplicateException
     */
    void addPackage(long userId, BIBusinessPackage biBusinessPackage) throws BIPackageDuplicateException;

    /**
     * 是否包含业务包
     *
     * @param userId    用户ID
     * @param biPackage 业务包
     * @return 是否包含
     */
    Boolean containPackage(long userId, BIBusinessPackage biPackage);

    /**
     * 根据ID判断是否包含业务包
     *
     * @param userId    用户ID
     * @param packageID 业务包ID
     * @return 是否包含
     */
    Boolean containPackageID(long userId, BIPackageID packageID);

    /**
     * 是否包含相应分组
     *
     * @param userId       用户ID
     * @param groupTagName 分组名
     * @return 是否包含
     */
    Boolean containGroup(long userId, BIGroupTagName groupTagName);

    /**
     * 移除相应业务包
     *
     * @param userId    用户ID
     * @param packageID 业务包ID
     * @throws BIPackageAbsentException 业务包缺少抛错
     */
    void removePackage(long userId, BIPackageID packageID) throws BIPackageAbsentException;

    /**
     * 移除分组
     *
     * @param userId       用户ID
     * @param groupTagName 分组名
     * @throws BIGroupAbsentException 分组缺少抛错
     */
    void removeGroup(long userId, BIGroupTagName groupTagName) throws BIGroupAbsentException;

    /**
     * 持久化数据
     * TODO 应该按照规则自动调用
     *
     * @param userId 用户ID
     */
    @Deprecated
    void persistData(long userId);

    /**
     * 创建空的分组
     *
     * @param userId       用户ID
     * @param groupTagName 分组名
     * @throws BIGroupDuplicateException 分组重复抛错
     */
    void createEmptyGroup(long userId, BIGroupTagName groupTagName, long position) throws BIGroupDuplicateException;

    /**
     * 给业务包打上分组标签
     *
     * @param userId       用户ID
     * @param packageID    业务包ID
     * @param groupTagName 分组名
     * @throws BIPackageAbsentException    业务包不存在抛错
     * @throws BIGroupAbsentException      分组不存在抛错
     * @throws BIPackageDuplicateException 业务包重复抛错
     */
    void stickGroupTagOnPackage(long userId, BIPackageID packageID, BIGroupTagName groupTagName) throws BIPackageAbsentException, BIGroupAbsentException, BIPackageDuplicateException;

    /**
     * 移除业务包分组标签
     *
     * @param userId       用户ID
     * @param packageID    业务包ID
     * @param groupTagName 分组名
     * @throws BIPackageAbsentException    业务包不存在抛错
     * @throws BIGroupAbsentException      分组不存在抛错
     * @throws BIPackageDuplicateException 业务包重复抛错
     */
    void removeGroupTagFromPackage(long userId, BIPackageID packageID, BIGroupTagName groupTagName) throws BIPackageAbsentException, BIGroupAbsentException, BIPackageDuplicateException;

    /**
     * 获得分组
     *
     * @param userId       用户ID
     * @param groupTagName 分组名
     * @return 分组
     * @throws BIGroupAbsentException 分组不存在
     */
    IBusinessGroupGetterService getGroup(long userId, BIGroupTagName groupTagName) throws BIGroupAbsentException;

    /**
     * 根据业务包名字获得业务包
     *
     * @param userId   用户ID
     * @param packName 业务包名字
     * @return 业务包
     */
    Set<BIBusinessPackage> getPackageByName(long userId, BIPackageName packName);

    /**
     * 标志开始构建Cube
     *
     * @param userId 用户ID
     */
    void startBuildingCube(long userId);

    /**
     * 标志Cube构建结束
     *
     * @param userId 用户ID
     */
    void endBuildingCube(long userId);

    /**
     * 标志Cube构建结束,同时当前还有哪些表是没有构建的
     *
     * @param userId 用户ID
     */
    void endBuildingCube(long userId, Set<CubeTableSource> absentTable);

    /**
     * 当前是否没有业务包
     *
     * @param userId 用户ID
     * @return 是否有
     */
    Boolean isPackagesEmpty(long userId);

    /**
     * 创建Group的Json数据
     *
     * @param userId 用户ID
     * @return json数据
     * @throws JSONException
     */
    JSONObject createGroupJSON(long userId) throws JSONException;

    /**
     * 解析Group的Json数据
     *
     * @param userId 用户ID
     * @return json数据
     * @throws JSONException
     */
    void parseGroupJSON(long userId, JSONObject jo) throws JSONException;

    /**
     * 创建业务包的Json数据
     *
     * @param userId 用户ID
     * @return json数据
     * @throws Exception
     */
    JSONObject createPackageJSON(long userId) throws Exception;

    /**
     * 根据locale创建业务包的Json数据
     *
     * @param locale 国际化
     * @param userId 用户ID
     * @return json数据
     * @throws Exception
     */
    JSONObject createPackageJSON(long userId, Locale locale) throws Exception;

    /**
     * 根据locale创建分析用的生成过cube的业务包Json数据
     *
     * @param userId
     * @param locale
     * @return
     */
    JSONObject createAnalysisPackageJSON(long userId, Locale locale) throws Exception;

    /**
     * 从指定业务包中移走指定表
     *
     * @param userId    用户ID
     * @param packageID 业务表ID
     * @param biTableID 表ID
     * @throws BIPackageAbsentException 业务包不存在抛错
     * @throws BITableAbsentException   表不存在抛错
     */
    void removeTable(long userId, BIPackageID packageID, BITableID biTableID) throws BIPackageAbsentException, BITableAbsentException;

    /**
     * 是否ID所对应的Package被打上分组标签
     *
     * @param userId    用户ID
     * @param packageID 业务包ID
     * @return 业务包是被填上标签
     */
    Boolean isPackageTaggedSomeGroup(long userId, BIPackageID packageID);

    /**
     * 是否ID所对应的Package被打上相应的分组标签
     *
     * @param userId       用户ID
     * @param packageID    业务包ID
     * @param groupTagName 分组名
     * @return 是否加上Tag
     */
    Boolean isPackageTaggedSpecificGroup(long userId, BIPackageID packageID, BIGroupTagName groupTagName) throws BIGroupAbsentException;

    /**
     * 获得全部配置部分的表
     *
     * @param userId 用户ID
     * @return 返回当前配置部分全部的表
     */
    Set<BusinessTable> getAllTables(long userId);

    /**
     * 获得全部在分析部分的表
     *
     * @param userId 用户ID
     * @return 返回当前分析部分的表
     */
    Set<BusinessTable> getAnalysisAllTables(long userId);

    Set<BIBusinessPackage> getPackages4CubeGenerate(long userId);

    /**
     * 当前的配置的表是否比分析部分的表，只少，没新增。
     *
     * @param userId 用户ID
     * @return
     */
    boolean isTableReduced(long userId);

    /**
     * 当前的配置的表是否比分析部分的表，只新增，没减少。
     *
     * @param userId 用户ID
     * @return
     */
    boolean isTableIncreased(long userId);

    /**
     * 当前的配置的表和分析部分的表相比，是否改变了？
     *
     * @param userId 用户ID
     * @return
     */
    boolean isTableNoChange(long userId);
}
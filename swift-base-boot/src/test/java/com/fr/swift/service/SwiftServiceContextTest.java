package com.fr.swift.service;

import com.fr.swift.SwiftContext;
import com.fr.swift.beans.factory.BeanFactory;
import com.fr.swift.boot.service.SwiftServiceContext;
import com.fr.swift.db.Where;
import com.fr.swift.executor.TaskProducer;
import com.fr.swift.executor.task.ExecutorTask;
import com.fr.swift.executor.task.impl.CollateExecutorTask;
import com.fr.swift.executor.task.impl.DeleteExecutorTask;
import com.fr.swift.executor.task.impl.RealtimeInsertExecutorTask;
import com.fr.swift.executor.task.impl.TruncateExecutorTask;
import com.fr.swift.result.SwiftResultSet;
import com.fr.swift.segment.SegmentKey;
import com.fr.swift.segment.SegmentLocationInfo;
import com.fr.swift.source.SourceKey;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import java.util.Collections;
import java.util.List;

/**
 * This class created on 2019/3/5
 *
 * @author Lucifer
 * @description
 */
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(MockitoJUnitRunner.class)
@PrepareForTest({SwiftContext.class, TaskProducer.class})
public class SwiftServiceContextTest {

    @Mock
    BeanFactory beanFactory;
    @Mock
    AnalyseService analyseService;
    @Mock
    HistoryService historyService;
    @Mock
    BaseService baseService;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(SwiftContext.class, TaskProducer.class);
        Mockito.when(SwiftContext.get()).thenReturn(beanFactory);
        Mockito.when(beanFactory.getBean(AnalyseService.class)).thenReturn(analyseService);
        Mockito.when(beanFactory.getBean(HistoryService.class)).thenReturn(historyService);
        Mockito.when(beanFactory.getBean(BaseService.class)).thenReturn(baseService);
    }

    @Test
    public void cleanMetaCache() {
        new SwiftServiceContext().cleanMetaCache(new String[0]);
        Mockito.verify(baseService).cleanMetaCache(Mockito.any(String[].class));
    }

    @Test
    public void getQueryResult() throws Exception {
        String queryJson = "queryJson";
        new SwiftServiceContext().getQueryResult(queryJson);
        Mockito.verify(analyseService).getQueryResult(queryJson);
    }

    @Test
    public void updateSegmentInfo() {
        SegmentLocationInfo locationInfo = Mockito.mock(SegmentLocationInfo.class);
        SegmentLocationInfo.UpdateType updateType = SegmentLocationInfo.UpdateType.ALL;
        new SwiftServiceContext().updateSegmentInfo(locationInfo, updateType);
        Mockito.verify(analyseService).updateSegmentInfo(locationInfo, updateType);
    }

    @Test
    public void removeSegments() {
        String clusterId = "ClusterId";
        SourceKey sourceKey = Mockito.mock(SourceKey.class);
        List<String> segmentKeys = Mockito.mock(List.class);
        new SwiftServiceContext().removeSegments(clusterId, sourceKey, segmentKeys);
        Mockito.verify(analyseService).removeSegments(clusterId, sourceKey, segmentKeys);
    }

    @Test
    public void insert() throws Exception {
        new SwiftServiceContext().insert(Mockito.mock(SourceKey.class), Mockito.mock(SwiftResultSet.class));
        PowerMockito.verifyStatic(TaskProducer.class);
        TaskProducer.produceTask(Mockito.any(RealtimeInsertExecutorTask.class));
    }

    @Test
    public void truncate() throws Exception {
        new SwiftServiceContext().truncate(Mockito.mock(SourceKey.class));
        PowerMockito.verifyStatic(TaskProducer.class);
        TaskProducer.produceTask(Mockito.any(TruncateExecutorTask.class));
    }

    @Test
    public void removeHistory() {
        List<SegmentKey> list = Mockito.mock(List.class);
        new SwiftServiceContext().removeHistory(list);
        Mockito.verify(historyService).removeHistory(list);
    }

    @Test
    public void index() {
        try {
            new SwiftServiceContext().index(null);
            Assert.assertTrue(false);
        } catch (UnsupportedOperationException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void curentStatus() {
        try {
            new SwiftServiceContext().currentStatus();
            Assert.assertTrue(false);
        } catch (UnsupportedOperationException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void appointCollate() throws Exception {
        new SwiftServiceContext().appointCollate(Mockito.mock(SourceKey.class), Mockito.mock(List.class));
        PowerMockito.verifyStatic(TaskProducer.class);
        TaskProducer.produceTask(Mockito.any(CollateExecutorTask.class));
    }

    @Test
    public void delete() throws Exception {
        new SwiftServiceContext().delete(Mockito.mock(SourceKey.class), Mockito.mock(Where.class));
        PowerMockito.verifyStatic(TaskProducer.class);
        TaskProducer.produceTask(Mockito.any(DeleteExecutorTask.class));
    }

    @Test
    public void upload() throws Exception {
        new SwiftServiceContext().upload(Collections.singleton(Mockito.mock(SegmentKey.class)));
        PowerMockito.verifyStatic(TaskProducer.class);
        TaskProducer.produceTasks(Mockito.<ExecutorTask>anySet());
    }

    @Test
    public void download() throws Exception {
        new SwiftServiceContext().download(Collections.singleton(Mockito.mock(SegmentKey.class)), true);
        PowerMockito.verifyStatic(TaskProducer.class);
        TaskProducer.produceTasks(Mockito.<ExecutorTask>anySet());
    }

    @Test
    public void uploadAllShow() throws Exception {
        new SwiftServiceContext().uploadAllShow(Collections.singleton(Mockito.mock(SegmentKey.class)));
        PowerMockito.verifyStatic(TaskProducer.class);
        TaskProducer.produceTasks(Mockito.<ExecutorTask>anySet());
    }

    @Test
    public void downloadAllShow() throws Exception {
        new SwiftServiceContext().downloadAllShow(Collections.singleton(Mockito.mock(SegmentKey.class)));
        PowerMockito.verifyStatic(TaskProducer.class);
        TaskProducer.produceTasks(Mockito.<ExecutorTask>anySet());
    }
}
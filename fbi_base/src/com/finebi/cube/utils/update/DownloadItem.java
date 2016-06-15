package com.finebi.cube.utils.update;

import com.fr.general.ComparatorUtils;

/**
 * @author richie
 * @date 2015-04-02
 * @since 8.0
 */
public class DownloadItem {

    //��ʾΪ�ٷֱ�
    private static final int PERCENTAGE_RATIO = 100;
    //��ʾkB
    private static final int BYTETOKB_RATIO = 1000;

    private String name;
    private String url;

    private int totalLength;
    private int downloadLength;

    public DownloadItem(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public int getTotalLength() {
        return totalLength;
    }

    public int getDownloadLength() {
        return downloadLength;
    }

    public void setTotalLength(int totalLength) {
        this.totalLength = totalLength;
    }

    public void setDownloadLength(int downloadLength) {
        this.downloadLength = downloadLength;
    }

    public int getProgressValue() {
        return (int) ((downloadLength / (double) totalLength) * PERCENTAGE_RATIO);
    }

    public String getProgressString() {
        return downloadLength / BYTETOKB_RATIO + "KB/" + totalLength / BYTETOKB_RATIO + "KB";
    }

    /**
     * ת��Ϊ�ַ���
     *
     * @return �ַ���
     */
    @Override
    public String toString() {
        return "name:" + name + ";download:" + getProgressString();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DownloadItem
                && ComparatorUtils.equals(((DownloadItem) obj).name, name)
                && ComparatorUtils.equals(((DownloadItem) obj).url, url);
    }

    /**
     * ����һ��hash��
     *
     * @return hash��
     */
    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
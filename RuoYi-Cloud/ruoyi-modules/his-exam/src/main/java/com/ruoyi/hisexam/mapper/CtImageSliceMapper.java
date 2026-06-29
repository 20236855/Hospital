package com.ruoyi.hisexam.mapper;

import java.util.List;
import com.ruoyi.hisexam.domain.CtImageSlice;

/**
 * CT影像切片Mapper接口
 *
 * @author ruoyi
 * @date 2026-06-26
 */
public interface CtImageSliceMapper
{
    /**
     * 查询CT影像切片
     */
    public CtImageSlice selectCtImageSliceBySliceId(Long sliceId);

    /**
     * 根据申请单ID查询切片列表
     */
    public List<CtImageSlice> selectCtImageSliceByApplyId(Long applyId);

    /**
     * 根据检查类型和申请单ID查询切片列表
     */
    public List<CtImageSlice> selectCtImageSliceList(CtImageSlice ctImageSlice);

    /**
     * 新增CT影像切片
     */
    public int insertCtImageSlice(CtImageSlice ctImageSlice);

    /**
     * 批量新增CT影像切片
     */
    public int batchInsertCtImageSlice(List<CtImageSlice> list);

    /**
     * 修改CT影像切片
     */
    public int updateCtImageSlice(CtImageSlice ctImageSlice);

    /**
     * 删除CT影像切片
     */
    public int deleteCtImageSliceBySliceId(Long sliceId);

    /**
     * 根据申请单ID删除切片
     */
    public int deleteCtImageSliceByApplyId(Long applyId);

    /**
     * 批量删除CT影像切片
     */
    public int deleteCtImageSliceBySliceIds(Long[] sliceIds);
}

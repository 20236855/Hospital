package com.ruoyi.hisexam.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.hisexam.domain.CtImageSlice;
import com.ruoyi.hisexam.mapper.CtImageSliceMapper;
import com.ruoyi.hisexam.service.ICtImageSliceService;

/**
 * CT影像切片Service业务层处理
 *
 * @author ruoyi
 * @date 2026-06-26
 */
@Service
public class CtImageSliceServiceImpl implements ICtImageSliceService
{
    @Autowired
    private CtImageSliceMapper ctImageSliceMapper;

    @Override
    public CtImageSlice selectCtImageSliceBySliceId(Long sliceId) {
        return ctImageSliceMapper.selectCtImageSliceBySliceId(sliceId);
    }

    @Override
    public List<CtImageSlice> selectCtImageSliceByApplyId(Long applyId) {
        return ctImageSliceMapper.selectCtImageSliceByApplyId(applyId);
    }

    @Override
    public List<CtImageSlice> selectCtImageSliceList(CtImageSlice ctImageSlice) {
        return ctImageSliceMapper.selectCtImageSliceList(ctImageSlice);
    }

    @Override
    public int insertCtImageSlice(CtImageSlice ctImageSlice) {
        return ctImageSliceMapper.insertCtImageSlice(ctImageSlice);
    }

    @Override
    public int batchInsertCtImageSlice(List<CtImageSlice> list) {
        return ctImageSliceMapper.batchInsertCtImageSlice(list);
    }

    @Override
    public int updateCtImageSlice(CtImageSlice ctImageSlice) {
        return ctImageSliceMapper.updateCtImageSlice(ctImageSlice);
    }

    @Override
    public int deleteCtImageSliceBySliceIds(Long[] sliceIds) {
        return ctImageSliceMapper.deleteCtImageSliceBySliceIds(sliceIds);
    }

    @Override
    public int deleteCtImageSliceByApplyId(Long applyId) {
        return ctImageSliceMapper.deleteCtImageSliceByApplyId(applyId);
    }
}

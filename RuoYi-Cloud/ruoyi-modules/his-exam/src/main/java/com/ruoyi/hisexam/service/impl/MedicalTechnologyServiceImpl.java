package com.ruoyi.hisexam.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.hisexam.mapper.MedicalTechnologyMapper;
import com.ruoyi.hisexam.domain.MedicalTechnology;
import com.ruoyi.hisexam.service.IMedicalTechnologyService;

/**
 * 医技项目Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-06-02
 */
@Service
public class MedicalTechnologyServiceImpl implements IMedicalTechnologyService 
{
    private static final LabSeed[] LAB_SEEDS = new LabSeed[] {
            new LabSeed("LAB_BLOOD_ROUTINE", "血常规检验", "血常规/验血", new BigDecimal("25.00"), 10L,
                    new String[] { "血常规检验", "血常规", "验血" }),
            new LabSeed("LAB_BIOCHEMISTRY", "生化全套检验", "肝肾功能/血糖血脂/生化检验", new BigDecimal("180.00"), 20L,
                    new String[] { "生化全套检验", "生化检验", "生化全套", "肝功能", "肾功能", "肝肾功能", "血糖血脂" }),
            new LabSeed("LAB_IMMUNOLOGY", "免疫学检验", "感染免疫/免疫球蛋白/补体", new BigDecimal("220.00"), 30L,
                    new String[] { "免疫学检验", "免疫检验", "免疫学", "感染免疫", "免疫球蛋白", "补体" })
    };

    @Autowired
    private MedicalTechnologyMapper medicalTechnologyMapper;

    /**
     * 查询医技项目
     * 
     * @param id 医技项目主键
     * @return 医技项目
     */
    @Override
    public MedicalTechnology selectMedicalTechnologyById(Long id)
    {
        return medicalTechnologyMapper.selectMedicalTechnologyById(id);
    }

    /**
     * 查询医技项目列表
     * 
     * @param medicalTechnology 医技项目
     * @return 医技项目
     */
    @Override
    public List<MedicalTechnology> selectMedicalTechnologyList(MedicalTechnology medicalTechnology)
    {
        return medicalTechnologyMapper.selectMedicalTechnologyList(medicalTechnology);
    }

    @Override
    public List<MedicalTechnology> selectLabTechnologyOptions()
    {
        List<MedicalTechnology> all = medicalTechnologyMapper.selectMedicalTechnologyList(new MedicalTechnology());
        for (LabSeed seed : LAB_SEEDS)
        {
            if (findLabSeed(all, seed) == null)
            {
                MedicalTechnology item = new MedicalTechnology();
                item.setTechCode(seed.code);
                item.setTechName(seed.name);
                item.setTechType("LAB");
                item.setTechFormat("次");
                item.setTechPrice(seed.price);
                item.setPriceType("检验费");
                item.setPyCode(seed.code);
                item.setSort(seed.sort);
                item.setStatus("0");
                item.setRemark(seed.remark);
                item.setCreateTime(DateUtils.getNowDate());
                medicalTechnologyMapper.insertMedicalTechnology(item);
                all.add(item);
            }
        }

        List<MedicalTechnology> result = new ArrayList<>();
        for (MedicalTechnology item : all)
        {
            if (isLabTechnology(item))
            {
                result.add(item);
            }
        }
        result.sort(Comparator.comparing(MedicalTechnology::getSort, Comparator.nullsLast(Long::compareTo))
                .thenComparing(MedicalTechnology::getId, Comparator.nullsLast(Long::compareTo)));
        return result;
    }

    /**
     * 新增医技项目
     * 
     * @param medicalTechnology 医技项目
     * @return 结果
     */
    @Override
    public int insertMedicalTechnology(MedicalTechnology medicalTechnology)
    {
        medicalTechnology.setCreateTime(DateUtils.getNowDate());
        return medicalTechnologyMapper.insertMedicalTechnology(medicalTechnology);
    }

    /**
     * 修改医技项目
     * 
     * @param medicalTechnology 医技项目
     * @return 结果
     */
    @Override
    public int updateMedicalTechnology(MedicalTechnology medicalTechnology)
    {
        medicalTechnology.setUpdateTime(DateUtils.getNowDate());
        return medicalTechnologyMapper.updateMedicalTechnology(medicalTechnology);
    }

    /**
     * 批量删除医技项目
     * 
     * @param ids 需要删除的医技项目主键
     * @return 结果
     */
    @Override
    public int deleteMedicalTechnologyByIds(Long[] ids)
    {
        return medicalTechnologyMapper.deleteMedicalTechnologyByIds(ids);
    }

    /**
     * 删除医技项目信息
     * 
     * @param id 医技项目主键
     * @return 结果
     */
    @Override
    public int deleteMedicalTechnologyById(Long id)
    {
        return medicalTechnologyMapper.deleteMedicalTechnologyById(id);
    }

    private MedicalTechnology findLabSeed(List<MedicalTechnology> all, LabSeed seed)
    {
        for (MedicalTechnology item : all)
        {
            String techName = normalize(item.getTechName());
            for (String alias : seed.aliases)
            {
                String target = normalize(alias);
                if (StringUtils.isNotEmpty(techName) && (techName.contains(target) || target.contains(techName)))
                {
                    return item;
                }
            }
        }
        return null;
    }

    private boolean isLabTechnology(MedicalTechnology item)
    {
        if (item == null)
        {
            return false;
        }
        String type = StringUtils.defaultString(item.getTechType()).toUpperCase(Locale.ROOT);
        if ("INSPEC".equals(type) || "LAB".equals(type))
        {
            return true;
        }
        for (LabSeed seed : LAB_SEEDS)
        {
            if (findLabSeed(Collections.singletonList(item), seed) != null)
            {
                return true;
            }
        }
        return false;
    }

    private String normalize(String value)
    {
        return StringUtils.defaultString(value).replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
    }

    private static class LabSeed
    {
        private final String code;
        private final String name;
        private final String remark;
        private final BigDecimal price;
        private final Long sort;
        private final String[] aliases;

        private LabSeed(String code, String name, String remark, BigDecimal price, Long sort, String[] aliases)
        {
            this.code = code;
            this.name = name;
            this.remark = remark;
            this.price = price;
            this.sort = sort;
            this.aliases = aliases;
        }
    }
}

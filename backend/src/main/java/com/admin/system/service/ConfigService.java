package com.admin.system.service;

import com.admin.system.common.exception.BusinessException;
import com.admin.system.dto.ConfigCreateRequest;
import com.admin.system.dto.ConfigQueryRequest;
import com.admin.system.dto.ConfigUpdateRequest;
import com.admin.system.entity.SysConfig;
import com.admin.system.mapper.SysConfigMapper;
import com.admin.system.vo.ConfigVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConfigService {

    private final SysConfigMapper sysConfigMapper;

    public IPage<ConfigVO> pageQuery(ConfigQueryRequest request) {
        Page<SysConfig> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getConfigType())) {
            wrapper.eq(SysConfig::getConfigType, request.getConfigType());
        }
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.and(w -> w.like(SysConfig::getConfigKey, request.getKeyword())
                    .or().like(SysConfig::getDescription, request.getKeyword()));
        }
        wrapper.orderByDesc(SysConfig::getUpdatedTime);
        IPage<SysConfig> configPage = sysConfigMapper.selectPage(page, wrapper);
        return configPage.convert(this::toVO);
    }

    public ConfigVO getById(Long id) {
        SysConfig config = sysConfigMapper.selectById(id);
        if (config == null) {
            throw new BusinessException("配置不存在");
        }
        return toVO(config);
    }

    public ConfigVO getByKey(String configKey) {
        SysConfig config = sysConfigMapper.selectOne(
                new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, configKey));
        if (config == null) {
            throw new BusinessException("配置不存在");
        }
        return toVO(config);
    }

    public ConfigVO create(ConfigCreateRequest request) {
        Long count = sysConfigMapper.selectCount(
                new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, request.getConfigKey()));
        if (count > 0) {
            throw new BusinessException("配置键已存在");
        }
        SysConfig config = new SysConfig();
        config.setConfigKey(request.getConfigKey());
        config.setConfigValue(request.getConfigValue());
        config.setConfigType(request.getConfigType());
        config.setDescription(request.getDescription());
        config.setCreatedTime(LocalDateTime.now());
        config.setUpdatedTime(LocalDateTime.now());
        sysConfigMapper.insert(config);
        return toVO(config);
    }

    public ConfigVO update(Long id, ConfigUpdateRequest request) {
        SysConfig config = sysConfigMapper.selectById(id);
        if (config == null) {
            throw new BusinessException("配置不存在");
        }
        if (request.getConfigValue() != null) {
            config.setConfigValue(request.getConfigValue());
        }
        if (request.getConfigType() != null) {
            config.setConfigType(request.getConfigType());
        }
        if (request.getDescription() != null) {
            config.setDescription(request.getDescription());
        }
        config.setUpdatedTime(LocalDateTime.now());
        sysConfigMapper.updateById(config);
        return toVO(config);
    }

    public void delete(Long id) {
        SysConfig config = sysConfigMapper.selectById(id);
        if (config == null) {
            throw new BusinessException("配置不存在");
        }
        sysConfigMapper.deleteById(id);
    }

    public Map<String, String> getConfigsByType(String type) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getConfigType, type);
        List<SysConfig> configs = sysConfigMapper.selectList(wrapper);
        return configs.stream()
                .collect(Collectors.toMap(SysConfig::getConfigKey, SysConfig::getConfigValue, (v1, v2) -> v2));
    }

    public void saveConfigsByType(String type, Map<String, String> configMap) {
        for (Map.Entry<String, String> entry : configMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysConfig::getConfigKey, key).eq(SysConfig::getConfigType, type);
            SysConfig existing = sysConfigMapper.selectOne(wrapper);
            if (existing != null) {
                existing.setConfigValue(value);
                existing.setUpdatedTime(LocalDateTime.now());
                sysConfigMapper.updateById(existing);
            } else {
                SysConfig config = new SysConfig();
                config.setConfigKey(key);
                config.setConfigValue(value);
                config.setConfigType(type);
                config.setCreatedTime(LocalDateTime.now());
                config.setUpdatedTime(LocalDateTime.now());
                sysConfigMapper.insert(config);
            }
        }
    }

    private ConfigVO toVO(SysConfig config) {
        ConfigVO vo = new ConfigVO();
        BeanUtils.copyProperties(config, vo);
        return vo;
    }
}

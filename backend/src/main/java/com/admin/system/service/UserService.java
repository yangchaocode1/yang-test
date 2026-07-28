package com.admin.system.service;

import com.admin.system.common.exception.BusinessException;
import com.admin.system.dto.*;
import com.admin.system.entity.SysRole;
import com.admin.system.entity.SysUser;
import com.admin.system.entity.SysUserRole;
import com.admin.system.mapper.SysRoleMapper;
import com.admin.system.mapper.SysUserMapper;
import com.admin.system.mapper.SysUserRoleMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务
 * 处理用户的增删改查、状态管理、密码管理、导入导出等业务逻辑
 */
@Service
@RequiredArgsConstructor
public class UserService {

    /** 用户Mapper，用于用户表的数据库操作 */
    private final SysUserMapper sysUserMapper;

    /** 角色Mapper，用于查询角色信息 */
    private final SysRoleMapper sysRoleMapper;

    /** 用户-角色关联Mapper，用于用户角色关系的数据库操作 */
    private final SysUserRoleMapper sysUserRoleMapper;

    /** 密码编码器，用于密码加密和验证 */
    private final PasswordEncoder passwordEncoder;

    /**
     * 分页查询用户列表
     * 支持关键词搜索（用户名、姓名、手机号、邮箱）、状态筛选、排序
     *
     * @param request 查询条件，含关键词、状态、分页参数、排序参数
     * @return 分页用户视图对象列表
     */
    public IPage<UserVO> listUsers(UserQueryRequest request) {
        Page<SysUser> page = new Page<>(request.getPageNum(), request.getPageSize());

        // 设置排序方式
        if ("ASC".equalsIgnoreCase(request.getOrderDirection())) {
            page.addOrder(OrderItem.asc(request.getOrderBy()));
        } else {
            page.addOrder(OrderItem.desc(request.getOrderBy()));
        }

        // 构建查询条件
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            wrapper.and(w -> w
                    .like(SysUser::getUsername, request.getKeyword())
                    .or().like(SysUser::getRealName, request.getKeyword())
                    .or().like(SysUser::getPhone, request.getKeyword())
                    .or().like(SysUser::getEmail, request.getKeyword()));
        }
        if (request.getStatus() != null) {
            wrapper.eq(SysUser::getStatus, request.getStatus());
        }

        IPage<SysUser> userPage = sysUserMapper.selectPage(page, wrapper);
        return userPage.convert(this::toUserVO);
    }

    /**
     * 根据ID获取用户详情
     *
     * @param id 用户ID
     * @return 用户视图对象
     * @throws BusinessException 用户不存在时抛出
     */
    public UserVO getUserById(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return toUserVO(user);
    }

    /**
     * 创建用户
     * 校验用户名唯一性，创建用户并关联角色
     *
     * @param request 创建用户请求
     * @return 创建后的用户视图对象
     * @throws BusinessException 用户名已存在时抛出
     */
    @Transactional
    public UserVO createUser(UserCreateRequest request) {
        // 校验用户名是否已存在
        Long count = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 构建用户实体
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        user.setExpireTime(request.getExpireTime());
        user.setMustChangePassword(0);
        user.setCreatedBy(getCurrentUserId());
        user.setCreatedTime(LocalDateTime.now());
        user.setUpdatedBy(getCurrentUserId());
        user.setUpdatedTime(LocalDateTime.now());

        sysUserMapper.insert(user);

        // 保存用户角色关联关系
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            saveUserRoles(user.getId(), request.getRoleIds());
        }

        return toUserVO(user);
    }

    /**
     * 更新用户
     * 更新用户基本信息和角色关联关系
     *
     * @param id      用户ID
     * @param request 更新用户请求
     * @return 更新后的用户视图对象
     * @throws BusinessException 用户不存在时抛出
     */
    @Transactional
    public UserVO updateUser(Long id, UserUpdateRequest request) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 仅更新非空字段
        if (request.getRealName() != null) {
            user.setRealName(request.getRealName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        if (request.getExpireTime() != null) {
            user.setExpireTime(request.getExpireTime());
        }
        user.setUpdatedBy(getCurrentUserId());
        user.setUpdatedTime(LocalDateTime.now());

        sysUserMapper.updateById(user);

        // 如果传入了角色ID列表，则先删除旧关联再保存新关联
        if (request.getRoleIds() != null) {
            sysUserRoleMapper.delete(
                    new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
            if (!request.getRoleIds().isEmpty()) {
                saveUserRoles(id, request.getRoleIds());
            }
        }

        return toUserVO(user);
    }

    /**
     * 删除用户
     * 逻辑删除用户，同时删除用户与角色的关联关系
     *
     * @param id 用户ID
     * @throws BusinessException 用户不存在时抛出
     */
    @Transactional
    public void deleteUser(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 删除用户
        sysUserMapper.deleteById(id);
        // 删除用户角色关联
        sysUserRoleMapper.delete(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
    }

    /**
     * 批量删除用户
     * 批量逻辑删除用户，同时删除用户与角色的关联关系
     *
     * @param ids 用户ID列表
     * @throws BusinessException ID列表为空时抛出
     */
    @Transactional
    public void batchDeleteUsers(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要删除的用户");
        }
        sysUserMapper.deleteBatchIds(ids);
        sysUserRoleMapper.delete(
                new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getUserId, ids));
    }

    /**
     * 切换用户状态
     * 启用/禁用用户，状态在0（禁用）和1（启用）之间切换
     *
     * @param id 用户ID
     * @throws BusinessException 用户不存在时抛出
     */
    @Transactional
    public void toggleUserStatus(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 状态切换：1->0 或 0->1
        user.setStatus(user.getStatus() == 1 ? 0 : 1);
        user.setUpdatedBy(getCurrentUserId());
        user.setUpdatedTime(LocalDateTime.now());
        sysUserMapper.updateById(user);
    }

    /**
     * 修改用户邮箱
     * 单独更新邮箱字段，校验邮箱格式和唯一性
     *
     * @param id      用户ID
     * @param request 修改邮箱请求，包含新邮箱
     * @return 更新后的用户视图对象
     * @throws BusinessException 用户不存在或邮箱已被占用时抛出
     */
    @Transactional
    public UserVO updateEmail(Long id, UpdateEmailRequest request) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 校验邮箱是否已被其他用户占用
        Long existCount = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getEmail, request.getEmail())
                        .ne(SysUser::getId, id));
        if (existCount > 0) {
            throw new BusinessException("邮箱已被其他用户占用");
        }

        user.setEmail(request.getEmail());
        user.setUpdatedBy(getCurrentUserId());
        user.setUpdatedTime(LocalDateTime.now());
        sysUserMapper.updateById(user);

        return toUserVO(user);
    }

    /**
     * 设置账号有效期
     *
     * @param id      用户ID
     * @param request 有效期请求，包含过期时间
     * @throws BusinessException 用户不存在时抛出
     */
    @Transactional
    public void setExpireTime(Long id, ExpireTimeRequest request) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setExpireTime(request.getExpireTime());
        user.setUpdatedBy(getCurrentUserId());
        user.setUpdatedTime(LocalDateTime.now());
        sysUserMapper.updateById(user);
    }

    /**
     * 重置密码
     * 将用户密码重置为默认值"123456"，并标记需要下次登录修改密码
     *
     * @param id 用户ID
     * @throws BusinessException 用户不存在时抛出
     */
    @Transactional
    public void resetPassword(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(passwordEncoder.encode("123456"));
        user.setMustChangePassword(1);
        user.setUpdatedBy(getCurrentUserId());
        user.setUpdatedTime(LocalDateTime.now());
        sysUserMapper.updateById(user);
    }

    /**
     * 修改密码
     * 验证旧密码正确后更新为新密码，并清除必须修改密码标记
     *
     * @param id      用户ID
     * @param request 修改密码请求，包含旧密码和新密码
     * @throws BusinessException 用户不存在或旧密码错误时抛出
     */
    @Transactional
    public void changePassword(Long id, ChangePasswordRequest request) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 验证旧密码是否正确
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(0);
        user.setUpdatedBy(getCurrentUserId());
        user.setUpdatedTime(LocalDateTime.now());
        sysUserMapper.updateById(user);
    }

    /**
     * 导出用户数据为Excel
     * 将所有用户数据按创建时间倒序导出为Excel文件
     *
     * @param outputStream 输出流，用于写入Excel文件
     * @throws IOException 文件写入异常
     */
    public void exportUsers(OutputStream outputStream) throws IOException {
        List<SysUser> users = sysUserMapper.selectList(
                new LambdaQueryWrapper<SysUser>().orderByDesc(SysUser::getCreatedTime));

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("用户数据");

            // 创建表头行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"用户名", "姓名", "手机号", "邮箱", "状态"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                // 设置表头加粗样式
                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            // 填充数据行
            int rowIndex = 1;
            for (SysUser user : users) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(user.getUsername() != null ? user.getUsername() : "");
                row.createCell(1).setCellValue(user.getRealName() != null ? user.getRealName() : "");
                row.createCell(2).setCellValue(user.getPhone() != null ? user.getPhone() : "");
                row.createCell(3).setCellValue(user.getEmail() != null ? user.getEmail() : "");
                row.createCell(4).setCellValue(user.getStatus() != null && user.getStatus() == 1 ? "启用" : "禁用");
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
        }
    }

    /**
     * 从Excel文件导入用户数据
     * 逐行读取Excel文件，校验用户名唯一性后创建用户
     *
     * @param file 上传的Excel文件
     * @return 导入结果，包含总数、成功数、失败数和失败详情
     * @throws IOException 文件读取异常
     */
    @Transactional
    public UserImportResult importUsers(MultipartFile file) throws IOException {
        UserImportResult result = new UserImportResult();
        List<UserImportResult.FailDetail> failDetails = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            int totalCount = sheet.getLastRowNum();
            int successCount = 0;
            int failCount = 0;

            // 从第二行开始读取（第一行为表头）
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                String username = getCellStringValue(row.getCell(0));
                String realName = getCellStringValue(row.getCell(1));
                String phone = getCellStringValue(row.getCell(2));
                String email = getCellStringValue(row.getCell(3));
                String statusStr = getCellStringValue(row.getCell(4));

                // 校验用户名不能为空
                if (username == null || username.isBlank()) {
                    failCount++;
                    failDetails.add(new UserImportResult.FailDetail(i + 1, username, "用户名不能为空"));
                    continue;
                }

                // 校验用户名是否已存在
                Long existCount = sysUserMapper.selectCount(
                        new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
                if (existCount > 0) {
                    failCount++;
                    failDetails.add(new UserImportResult.FailDetail(i + 1, username, "用户名已存在"));
                    continue;
                }

                // 构建用户实体并插入
                SysUser user = new SysUser();
                user.setUsername(username);
                user.setPassword(passwordEncoder.encode("123456"));
                user.setRealName(realName);
                user.setPhone(phone);
                user.setEmail(email);
                user.setStatus("禁用".equals(statusStr) ? 0 : 1);
                user.setMustChangePassword(1);
                user.setCreatedBy(getCurrentUserId());
                user.setCreatedTime(LocalDateTime.now());
                user.setUpdatedBy(getCurrentUserId());
                user.setUpdatedTime(LocalDateTime.now());

                sysUserMapper.insert(user);
                successCount++;
            }

            result.setTotalCount(totalCount);
            result.setSuccessCount(successCount);
            result.setFailCount(failCount);
            result.setFailDetails(failDetails);
        }

        return result;
    }

    /**
     * 保存用户角色关联关系
     *
     * @param userId  用户ID
     * @param roleIds 角色ID列表
     */
    private void saveUserRoles(Long userId, List<Long> roleIds) {
        for (Long roleId : roleIds) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            sysUserRoleMapper.insert(userRole);
        }
    }

    /**
     * 将用户实体转换为用户视图对象
     * 包含用户基本信息和关联的角色列表
     *
     * @param user 用户实体
     * @return 用户视图对象
     */
    private UserVO toUserVO(SysUser user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setAvatar(user.getAvatar());
        vo.setStatus(user.getStatus());
        vo.setExpireTime(user.getExpireTime());
        vo.setMustChangePassword(user.getMustChangePassword());
        vo.setCreatedTime(user.getCreatedTime());
        vo.setUpdatedTime(user.getUpdatedTime());

        // 查询用户关联的角色信息
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, user.getId()));
        if (!userRoles.isEmpty()) {
            List<Long> roleIds = userRoles.stream()
                    .map(SysUserRole::getRoleId)
                    .collect(Collectors.toList());
            List<SysRole> roles = sysRoleMapper.selectBatchIds(roleIds);
            vo.setRoles(roles.stream().map(role -> {
                UserVO.RoleVO roleVO = new UserVO.RoleVO();
                roleVO.setId(role.getId());
                roleVO.setRoleCode(role.getRoleCode());
                roleVO.setRoleName(role.getRoleName());
                return roleVO;
            }).collect(Collectors.toList()));
        } else {
            vo.setRoles(List.of());
        }

        return vo;
    }

    /**
     * 获取当前登录用户ID
     * 从安全上下文中提取当前登录用户的ID
     *
     * @return 当前用户ID，未登录返回null
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof com.admin.system.security.LoginUser loginUser) {
            return loginUser.getSysUser().getId();
        }
        return null;
    }

    /**
     * 获取Excel单元格的字符串值
     * 处理不同类型的单元格（字符串、数字、布尔值），统一转换为字符串
     *
     * @param cell Excel单元格
     * @return 单元格的字符串值，无法转换时返回null
     */
    private String getCellStringValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double val = cell.getNumericCellValue();
                // 如果是整数则转为long避免小数点
                if (val == Math.floor(val) && !Double.isInfinite(val)) {
                    yield String.valueOf((long) val);
                }
                yield String.valueOf(val);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }

    /**
     * 获取当前登录用户信息
     * 从安全上下文中获取当前登录用户的详细信息
     *
     * @return 当前登录用户的视图对象
     * @throws BusinessException 未登录时抛出
     */
    public UserVO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof com.admin.system.security.LoginUser loginUser) {
            return toUserVO(loginUser.getSysUser());
        }
        throw new BusinessException("未登录");
    }

    /**
     * 批量创建测试用户
     * 生成指定数量的测试用户，用于测试数据
     *
     * @param count 要创建的测试用户数量
     */
    @Transactional
    public void batchCreateTestUsers(Integer count) {
        if (count == null || count <= 0) {
            throw new BusinessException("创建数量必须大于0");
        }

        LocalDateTime now = LocalDateTime.now();
        Long creatorId = getCurrentUserId() != null ? getCurrentUserId() : 1L;

        for (int i = 1; i <= count; i++) {
            SysUser user = new SysUser();
            user.setUsername("test_user_" + i);
            user.setPassword(passwordEncoder.encode("123456"));
            user.setRealName("测试用户" + i);
            user.setPhone("1380013800" + (i % 10));
            user.setEmail("test" + i + "@example.com");
            user.setStatus(1); // 启用状态
            user.setMustChangePassword(1);
            user.setCreatedBy(creatorId);
            user.setCreatedTime(now);
            user.setUpdatedBy(creatorId);
            user.setUpdatedTime(now);

            sysUserMapper.insert(user);
        }
    }

    /**
     * 清空测试用户
     * 删除所有以"test_user_"开头的测试用户
     */
    @Transactional
    public void clearTestUsers() {
        sysUserMapper.delete(
                new LambdaQueryWrapper<SysUser>()
                        .likeRight(SysUser::getUsername, "test_user_")
        );
    }

    /**
     * 检查用户是否拥有指定权限
     * 从安全上下文中获取当前用户的权限列表并检查
     *
     * @param permission 权限编码
     * @return 是否拥有该权限
     * @throws BusinessException 未登录时抛出
     */
    // public boolean hasPermission(String permission) {
    //     Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    //     if (authentication != null && authentication.getPrincipal() instanceof com.admin.system.security.LoginUser loginUser) {
    //         return loginUser.getPermissions().contains(permission);
    //     }
    //     throw new BusinessException("未登录");
    // }
}

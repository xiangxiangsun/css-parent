package css.security.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import css.security.common.enums.ExceptionEnum;
import css.security.common.exception.CssException;
import css.security.dao.PermissionDao;
import css.security.dao.RoleDao;
import css.security.dao.UserDao;
import css.security.dto.SysUserDTO;
import css.security.entity.PageResult;
import css.security.entity.Permission;
import css.security.entity.SysRole;
import css.security.entity.SysUser;
import css.security.service.UserService;
import css.security.utils.BeanHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@Transactional
public class UserServiceImpl implements UserService {

    @Resource
    UserDao userDao;

    @Resource
    RoleDao roleDao;

    @Resource
    PermissionDao permissionDao;

    @Override
    public SysUser loadUserByUsername(String username) {
        //根据用户名查询用户信息
        SysUser user = userDao.findByUserName(username);
        //根据用户信息查询用户所有的角色
        if(null != user){
            Set<SysRole> roles = roleDao.findRolesByUserId(user.getId());
            if(null != roles && roles.size() > 0){
                for (SysRole role : roles) {
                    //根据角色查询角色对应的权限
                    Set<Permission> permissions = permissionDao.findPermissionsByRoleId(role.getId());
                    role.setPermissions(permissions);
                }
            }
            user.setRoles(roles);
        }
        return user;
    }

    @Override
    public PageResult<SysUserDTO> findPage(Integer currentPage, Integer pageSize, String queryString) {
        System.out.println(currentPage+","+pageSize+","+queryString);
        //分页
        PageHelper.startPage(currentPage,pageSize);
        //查询表数据
        List<SysUser> userList = null;
        if (queryString == null || queryString.length() == 0) {
            userList = userDao.findUserPage();
        }else {
            userList = userDao.findPage(queryString);
        }
        if(CollectionUtils.isEmpty(userList)){
            throw new CssException(ExceptionEnum.INSERT_OPTIONS_ERROR);
        }
        //解析分页结果
        //自动计算：总元素个数，以及总页数
        PageInfo<SysUser> pageInfo = new PageInfo<>(userList);
        System.out.println("查询到数据："+userList.size());
        System.out.println("总个数："+pageInfo.getTotal());
        //转为SysUserDTO,减少流量传输
        List<SysUserDTO> list = BeanHelper.copyWithCollection(userList,SysUserDTO.class);
        //返回：个数，总页数，当前页数据
        return new PageResult<>(pageInfo.getTotal(),pageInfo.getPages(),list);
    }

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void add(SysUser user, Integer[] roleIds) {
        //初始密码默认123456，并用盐值加密
//        String password = passwordEncoder.encode("123456");
        //先将用户基本信息进行添加
        userDao.add(user);
        //通过uesrIds回显id
        setUserAndRole(user.getId(),roleIds);
    }

    @Override
    public SysUser findById(Integer id) {
        return userDao.findById(id);
    }

    @Override
    public List<Integer> findRoleIdsByUserId(Integer id) {
        return userDao.findRoleIdsByUserId(id);
    }

    @Override
    public void edit(SysUser user, Integer[] roleIds) {
        //先将用户基本信息进行修改
        userDao.edit(user);
        //通过userId对已有中间表进行删除
        userDao.deleteRoleIdByUserId(user.getId());
        //再次添加
        setUserAndRole(user.getId(),roleIds);
    }

    @Override
    public void deleteById(Integer id) {
        //先删除中价表关系
        userDao.deleteRoleIdByUserId(id);
        //再删除user表基本信息
        userDao.deleteByUserId(id);
    }

    public void setUserAndRole(Integer userId, Integer[] roleIds) {
        if (roleIds != null && roleIds.length>0){
            for (Integer roleId : roleIds) {
                Map<String,Integer> map = new HashMap<>();
                map.put("user_id",userId);
                map.put("role_id",roleId);
                userDao.setUserAndRole(map);
            }
        }
    }
}
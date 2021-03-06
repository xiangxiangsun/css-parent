package css.security.security;

import css.security.entity.SysRole;
import css.security.entity.SysUser;
import css.security.entity.SysUserRole;
import css.security.service.RoleSecurityService;
import css.security.service.UserRoleService;
import css.security.service.UserSecurityService;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service("userDetailsService")
public class CustomUserDetailsService implements UserDetailsService {
    @Resource
    private UserSecurityService userSecurityService;

    @Resource
    private RoleSecurityService roleService;

    @Resource
    private UserRoleService userRoleService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        // 从数据库中取出用户信息
        SysUser user = userSecurityService.selectByName(username);

        // 判断用户是否存在
        if(user == null) {
            throw new UsernameNotFoundException("用户名不存在");
        }

/*        // 添加权限(一个用户多个角色)
        List<SysUserRole> userRoles = userRoleService.listByUserId(user.getId());
                for (SysUserRole sysUserRole : userRoles) {
            SysRole role = roleService.selectById(sysUserRole.getRoleId());
            authorities.add(new SimpleGrantedAuthority(role.getName()));
        }*/

        //添加权限，一个用户仅一个角色
        String role = userRoleService.selectByUserId(user.getId());

        // 返回UserDetails实现类
        return new LoginUser(user.getUsername(),user.getPassword(),role,true,true,true,true);
    }
}

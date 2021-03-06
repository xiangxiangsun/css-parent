package css.security.dao;

import com.github.pagehelper.Page;
import css.security.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
public interface RoleDao {
    Set<SysRole> findRolesByUserId(@Param("userId") Integer userId);

    Page<SysRole> findPage(@Param("queryString") String queryString);

    void add(SysRole role);

    void addRoleAndMenu(@Param("maps") List<Map> maps);

    void addRoleAndPermission(@Param("mapList") List<Map> mapList);

    void edit(@Param("role") SysRole role);

    void deleteRoleAndMenu(@Param("id") Integer id);

    void deleteRoleAndPermission(@Param("id")Integer id);

    void delete(@Param("id") Integer id);

    List<SysRole> findAll();

    void setMenuIdByRoleId(Map<String, Integer> map);

    void setPermissionIdByRoleId(Map<String, Integer> map);

    SysRole findById(@Param("id") Integer id);

    List<Integer> findMenuIdsByRoleId(@Param("id")Integer id);

    List<Integer> findPermissionIdsByRoleId(@Param("id")Integer id);

//    Integer getRoleId(String name);
}

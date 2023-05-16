package club.hue.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;

@Mapper
@Repository
public interface LoginMapper {

    @Insert("insert into users(userid, username, password, rolenum, email) values((select lid from (select convert(cast(userid as signed)+1,char) as lid from users order by userid desc limit 1) as ta), #{username}, #{password}, #{rolenum}, #{email})")
    Integer addUser(@Param("username") String username, @Param("password") String password, @Param("rolenum") Integer rolenum, @Param("email") String email);

    @Select("select userid, rolenum from users where (userid=#{userid} or isnull(#{userid})) and (email=#{email} or isnull(#{email})) and password=#{password} limit 1")
    HashMap<String, Object> getUser(@Param("userid") String userid, @Param("email") String email, @Param("password") String password);

    @Select("select 1 from users where email=#{email} limit 1")
    Integer getUserByEmail(@Param("email") String email);

    @Select("select userid from users where email=#{email} limit 1")
    String getLastUserid(String email);

    @Select("select username, userid, rolenum, create_time, email from users where userid=#{userid}")
    HashMap<String, String> getUserProfile(String userid);

    // rolenum, rolename 对应关系
    @Select("select rolenum, rolename from role_pattern")
    List<HashMap<String, Object>> getRolePattern();

    @Insert("insert into login_records(id, ip_address, userid, device_type) values(uuid(), #{ip_address}, #{userid}, #{device_type})")
    Integer addLoginRecord(@Param("ip_address") String realIPAddress, @Param("userid") String userid, @Param("device_type") String deviceType);
}

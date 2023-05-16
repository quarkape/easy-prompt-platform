package club.hue.mapper;

import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;

@Mapper
@Repository
public interface PatternMapper {

    @Select("select ta.id, ta.name, field, username as creator, ta.create_time, tc.name as fieldName, type from " +
            "patterns as ta " +
            "left join " +
            "users as tb on ta.creator=tb.userid " +
            "left join " +
            "(select id, name from pattern_field) as tc on ta.field=tc.id " +
            "where (field=#{field} or #{field}='') and (#{keywords}='' or ta.name like concat('%', #{keywords}, '%')) and public=1 " +
            "order by ta.create_time desc " +
            "limit #{page}, #{count}")
    List<HashMap<String, String>> getPatternList(@Param("field") String field, @Param("keywords") String keywords, @Param("page") int page, @Param("count") int count);

    @Select("select id, name from pattern_field order by id asc")
    List<HashMap<String, String>> getPatternField();

    @Select("select count(*) from patterns where (field=#{field} or #{field}='') and (#{keywords}='' or name like concat('%', #{keywords}, '%')) and public=1")
    int getPatternCount(@Param("field") String field, @Param("keywords") String keywords);

    @Select("select ta.id, ta.name, ta.desc, ta.public, tb.id as field, username as creator, ta.create_time, config " +
            "from patterns as ta " +
            "left join " +
            "(select * from pattern_field) as tb on ta.field=tb.id " +
            "left join " +
            "(select * from pattern_config) as tc on ta.id=tc.id " +
            "left join " +
            "(select userid, username from users) as td on ta.creator=td.userid " +
            "where ta.id=#{id} and public=1 " +
            "limit 1")
    HashMap<String, Object> getPattern(String id);

    @Select("select ta.id, public, ta.name, ta.desc, tb.id as field, username as creator, ta.create_time, config from " +
            "patterns as ta " +
            "left join " +
            "(select * from pattern_field) as tb on ta.field=tb.id " +
            "left join " +
            "(select * from pattern_config) as tc on ta.id=tc.id " +
            "left join " +
            "(select userid, username from users) as td on ta.creator=td.userid " +
            "where ta.id=#{patternid} and owner=#{userid} " +
            "limit 1")
    HashMap<String, Object> getMyPatternInfo(@Param("patternid") String patternid, @Param("userid") String userid);

    @Select("select ta.id, ta.name, field, username as creator, ta.create_time, tc.name as fieldName, type from " +
            "patterns as ta " +
            "left join " +
            "users as tb on ta.creator=tb.userid " +
            "left join " +
            "(select id, name from pattern_field) as tc on ta.field=tc.id " +
            "where (field=#{field} or #{field}='') and (#{keywords}='' or ta.name like concat('%', #{keywords}, '%')) and ta.owner=#{ownerid} " +
            "order by ta.create_time desc")
    List<HashMap<String, Object>> getMyPattern(@Param("ownerid") String ownerid, @Param("field") String field, @Param("keywords") String keywords);

    @Select("select count(*) from patterns where (field=#{field} or #{field}='') and (#{keywords}='' or name like concat('%', #{keywords}, '%')) and owner=#{owner}")
    Integer getMyPatternCount(@Param("owner") String owner, @Param("field") String field, @Param("keywords") String keywords);

    @Select("select * from pattern_config where id=#{id}")
    HashMap<String, Object> getPatternConfig(String id);

    @Insert("insert into patterns(id, `name`, `desc`, field, creator, owner, public, type) " +
            "select #{newpatternid} as id, `name`, `desc` ,field, creator, #{ownerid}, 0 as public, 1 as type from patterns " +
            "where id=#{patternid} limit 1")
    Integer addPattern(@Param("newpatternid") String newpatternid, @Param("ownerid") String ownerid, @Param("patternid") String patternid);

    @Insert("insert into pattern_config(id, config) select #{uuid} as `id`, config from pattern_config where id = #{patternid} limit 1")
    Integer addPatternConfig(@Param("uuid") String uuid, @Param("patternid") String patternid);

    @Select("select (select count(*) from patterns where owner=#{owner}) > (select value from configs where name='collection_limit' limit 1)")
    Integer checkPatternLimitation(String owner);

//    @Update("update pattern_config set config=#{config} where id=#{id}")
//    Integer modifyPatternConfig(@Param("config") String config, @Param("id") String id);

//    @Update("update patterns set name=#{name}, `desc`=#{desc}, field=#{field}, public=#{ispublic} where id=#{id} and owner=#{userid}")
//    Integer modifyPatternInfoUpdate(@Param("userid") String userid, @Param("name") String name, @Param("desc") String desc, @Param("field") String field, @Param("ispublic") Integer ispublic, @Param("id") String id);

    // 模板信息的增改一体
    // 修改之前判断是不是自己的模板，不是的话不给修改
    @Insert("insert into patterns(id, `name`, `desc`, field, creator, `owner`, public, `type`) " +
            "values(#{id}, #{name}, #{desc}, #{field}, #{creator}, #{creator}, #{ispublic}, #{type}) " +
            "on duplicate key update `name`=#{name}, `desc`=#{desc}, field=#{field}, public=#{ispublic}")
    Integer modifyPatternInfo(@Param("id") String id, @Param("name") String name, @Param("desc") String desc, @Param("creator") String creator, @Param("field") String field, @Param("ispublic") Integer ispublic, @Param("type") Integer type);

    // 检查修改模板主要信息的人是不是模板的拥有者
    @Select("select 1 from " +
            "patterns as ta " +
            "left join " +
            "(select id from pattern_config) as tb " +
            "on ta.id=tb.id " +
            "where ta.id=#{patternid} and ta.owner=#{userid} " +
            "limit 1")
    Integer checkPatternOwnership(@Param("patternid") String patternid, @Param("userid") String userid);

    // 模板配置内容的增改一体
    // 修改之前判断是不是自己的模板，不是的话不给修改
    @Insert("insert into pattern_config(id, config) " +
            "values(#{id}, #{config}) " +
            "on duplicate key " +
            "update config=if((select owner from pattern_config as ta left join (select * from patterns) as tb " +
            "on ta.id=tb.id where ta.id=#{id} limit 1)=#{userid}, #{config}, config)")
    Integer modifyPatternConfig(@Param("config") String config, @Param("id") String id, @Param("userid") String userid);


//    @Delete("delete from patterns where id=#{id} and owner=#{userid}")
//    Integer deletePattern(@Param("id") String id, @Param("userid") String userid);
//
//    @Delete("delete from pattern_config where id=#{id} and (select owner from patterns as ta left join (select id from pattern_config) as tb on ta.id=tb.id where ta.id=#{id})=#{userid}")
//    Integer deletePattternConfig(@Param("id") String id, @Param("userid") String userid);

    @Delete("delete patterns, pattern_config from patterns join pattern_config on patterns.id = pattern_config.id where patterns.id=#{patternid} and patterns.owner=#{ownerid}")
    Integer deletePattern(@Param("patternid") String patternid, @Param("ownerid") String ownerid);

    // 获取提示模板示例
    @Select("select * from patterns as ta left join (select * from pattern_config) as tb on ta.id=tb.id where ta.owner=#{owner} limit 1")
    HashMap<String, Object> getPatternExample(String owner);

}

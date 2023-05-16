package club.hue.mapper;

import com.alibaba.fastjson.JSONArray;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.sql.Array;

@Mapper
@Repository
public interface PromptMapper {

    @Insert("insert into chat_records(id, creator, content) values(#{id}, #{creator}, #{content})")
    Integer initChatItem(@Param("id") String id, @Param("creator") String creator, @Param("content") String content);

    @Select("update chat_records set content=(select content from (select json_array_append(content, '$', JSON_ARRAY(cast(#{ja} as JSON), cast(#{jb} as JSON))) as content from chat_records where id=#{id}) as ta) where id=#{id}")
    Integer addChatItem(@Param("ja") String ja, @Param("jb") String jb, @Param("id") String id);
}

package club.hue.vo;

import lombok.Data;

@Data
public class ResultVOUtil {

    public static ResultVO oks() {
        ResultVO resultVO = new ResultVO();
        resultVO.setData(null);
        resultVO.setCode(0);
        resultVO.setMessage("ok");
        return resultVO;
    }

    public static ResultVO ok(Object object) {
        ResultVO resultVO = new ResultVO();
        resultVO.setData(object);
        resultVO.setCode(0);
        resultVO.setMessage("ok");
        return resultVO;
    }

    public static ResultVO err(Integer code, String message) {
        ResultVO resultVO = new ResultVO();
        resultVO.setCode(code);
        resultVO.setMessage(message);
        resultVO.setData(null);
        return resultVO;
    }

}

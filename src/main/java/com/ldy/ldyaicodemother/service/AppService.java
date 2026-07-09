package com.ldy.ldyaicodemother.service;

import com.ldy.ldyaicodemother.model.dto.app.AppQueryRequest;
import com.ldy.ldyaicodemother.model.entity.User;
import com.ldy.ldyaicodemother.model.vo.AppVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.ldy.ldyaicodemother.model.entity.App;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author ldy
 */
public interface AppService extends IService<App> {

    AppVO getAppVO(App app);

    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    List<AppVO> getAppVOList(List<App> appList);

    Flux<String> chatToGenCode(Long appId, String message, User loginUser);
}

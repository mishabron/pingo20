package com.mbronshteyn.pingo20.network;

import com.mbronshteyn.gameserver.dto.game.AuthinticateDto;
import com.mbronshteyn.gameserver.dto.game.CardDto;
import com.mbronshteyn.gameserver.dto.game.CardHitDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface PingoRemoteService {

    @POST("/pingo/Game/authinticate")
    Call<CardDto> authinticate(@Body AuthinticateDto authinticateDto);

    @POST("/pingo/Game/hit")
    Call<CardDto> hitCard(@Body CardHitDto cardHitDto);

    @GET("/pingo/Game/winningPin/{game}/{cardNo}/{deviceId}")
    Call<String> getWinningPin(@Path("game") String game,@Path("cardNo") Long cardNo,@Path("deviceId") String deviceId);

}

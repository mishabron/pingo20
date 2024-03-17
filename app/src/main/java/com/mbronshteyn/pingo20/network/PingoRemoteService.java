package com.mbronshteyn.pingo20.network;

import com.mbronshteyn.gameserver.dto.game.AuthinticateDto;
import com.mbronshteyn.gameserver.dto.game.CardDto;
import com.mbronshteyn.gameserver.dto.game.CardHitDto;
import com.mbronshteyn.gameserver.dto.game.HistoryDto;
import com.mbronshteyn.gameserver.dto.game.WinnerEmailDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface PingoRemoteService {

    public static String baseUrl = "https://pingo.win:8084";

    @POST("/pingo/Game/authinticate")
    Call<CardDto> authinticate(@Body AuthinticateDto authinticateDto);

    @POST("/pingo/Game/hit")
    Call<CardDto> hitCard(@Body CardHitDto cardHitDto);

    @GET("/pingo/Game/winningPin/{game}/{cardNo}/{deviceId}")
    Call<String> getWinningPin(@Path("game") String game,@Path("cardNo") Long cardNo,@Path("deviceId") String deviceId);

    @POST("/pingo/Game/sendEmail")
    Call<Void> saveEmail(@Body WinnerEmailDto winnerEmailDto);

    @POST("/pingo/Game/history")
    Call<HistoryDto> getHistory(@Body AuthinticateDto authinticateDto);

    @POST("/pingo/Game/freeAttempt")
    Call<CardDto> saveFreeAttempt(@Body AuthinticateDto authinticateDto);

}

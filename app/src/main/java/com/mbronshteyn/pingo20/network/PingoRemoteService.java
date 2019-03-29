package com.mbronshteyn.pingo20.network;

import com.mbronshteyn.gameserver.dto.game.AuthinticateDto;
import com.mbronshteyn.gameserver.dto.game.CardDto;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface PingoRemoteService {

    @POST("/pingo/Game/authinticate")
    Call<CardDto> authinticate(@Body AuthinticateDto authinticateDto);
}

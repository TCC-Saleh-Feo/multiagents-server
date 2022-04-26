package com.tccsafeo;

import com.tccsafeo.entities.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ManageAgentsService
{
    private HelloWorldAgent _helloWorldAgent;

    public ManageAgentsService(HelloWorldAgent helloWorldAgent) {
        this._helloWorldAgent = helloWorldAgent;
    }


    private List<Player> _serializeJsonArrayToPlayers(JSONArray jsonFileArray) {
        if (jsonFileArray != null) {
            List<Player> players = new ArrayList<>();
            jsonFileArray.stream().forEach(it -> {
                JSONObject jsonObject = (JSONObject) it;
                Long rating = (Long) jsonObject.get("Standard_Rating");
                players.add(new Player(jsonObject.get("Fide id").toString(), jsonObject.get("Name").toString(),
                        jsonObject.get("Federation").toString(), rating.intValue()));
            });
            return players;
        }
        return null;
    }

    private JSONArray _readJsonPlayer() {
        JSONParser parser = new JSONParser();
        try {
            JSONArray jsonFileArray = (JSONArray) parser.parse(new FileReader("/home/saleh/Documents/UnB/TCC/multiagents-server/mocks/womanChess.json"));
            return jsonFileArray;
        } catch (FileNotFoundException e){
            System.out.println("File Not Found");
        } catch (IOException | ParseException e) {
            System.out.println("Failed occurs to open file");
        }
        return null;
    }

    /**
     * Access the JSON data and
     * Get random players to make a Lobby
     *
     * @return randomPlayers: List<Player> - A random list of Players;
     */
    public List<Player> getPlayersReadyToPlay()
    {
        JSONArray jsonArray = _readJsonPlayer();
        List<Player> players = _serializeJsonArrayToPlayers(jsonArray);
        Random random = new Random();
        List<Player> randomPlayers = new ArrayList<>();
        Integer playersToQueueSize = random.nextInt(21);
        for (int i = 0; i < playersToQueueSize; i++) {
            int randomIndex = random.nextInt(players.size());
            randomPlayers.add(players.get(randomIndex));
            players.remove(randomIndex);
        }
        return randomPlayers;
    }
}

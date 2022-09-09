package com.tccsafeo.utils;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.IOException;
import java.util.ArrayList;

import com.tccsafeo.persistence.entities.Player;

public class Messenger {
    public static MessageTemplate sendMessage(
            Agent senderAgent,
            ArrayList<AID> receivers,
            String conversationId,
            String messageContent,
            Integer messageType
    ) {
        ACLMessage cfp = new ACLMessage(messageType);
        for(AID receiver: receivers) {
            cfp.addReceiver(receiver);
        }
        cfp.setConversationId(conversationId);
        cfp.setReplyWith("cfp" + System.currentTimeMillis());
        cfp.setContent(messageContent);
        senderAgent.send(cfp);
        return MessageTemplate.and(
                MessageTemplate.MatchConversationId(conversationId),
                MessageTemplate.MatchInReplyTo(cfp.getReplyWith())
        );
    }

    public static MessageTemplate sendPlayerOffer(Agent senderAgent, ArrayList<AID> receivers, Player player) {
        return sendMessage(
                senderAgent,
                receivers,
                "offering-player-" + player.playerId,
                JsonParser.toJson(player),
                ACLMessage.CFP
        );
    }
}

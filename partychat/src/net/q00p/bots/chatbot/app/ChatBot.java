package net.q00p.bots.chatbot.app;

import net.q00p.bots.Bot;
import net.q00p.bots.Message;
import net.q00p.bots.User;
import net.q00p.bots.chatbot.ChatContext;
import net.q00p.bots.chatbot.ChatLine;
import net.q00p.bots.chatbot.ChatLineManager;
import net.q00p.bots.chatbot.Subscriber;
import net.q00p.bots.chatbot.SubscriberManager;
import net.q00p.bots.chatbot.listeners.ChatListener;
import net.q00p.bots.io.Logger;
import net.q00p.bots.util.AbstractBot;
import net.q00p.bots.util.ServiceRegistrar;
import net.q00p.bots.util.Tuple;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChatBot extends AbstractBot {
	
	private ChatBot(String name) {
		ServiceRegistrar.get().register(
				SubscriberManager.class, new SubscriberManager());
		ServiceRegistrar.get().register(
				ChatLineManager.class, new ChatLineManager());
	}
	
	@Override
	public void handleMessage(Message message) {
		Subscriber subscriber = 
			ServiceRegistrar.get().service(SubscriberManager.class)
			.lookupMap.get(new Tuple<User,String>(message.getFrom(), message.getTo().getName()));
		ChatLine chatLine =
			ServiceRegistrar.get().service(ChatLineManager.class).getChatLine(subscriber);
		ChatContext context = new ChatContext(chatLine, subscriber);
		for (ChatListener chatListener : listeners) {
			chatListener.handleMessage(message, context);
		}
	}
	
	private Set<ChatListener> listeners = new HashSet<ChatListener>();

	public static void main(String[] args) {
		List<String> argList = Arrays.asList(args);
		assert argList.size() > 2 : 
			"usage: java PartyBot botName usn pwd usn pwd ...";
		Bot bot = new ChatBot(argList.get(0));
		Logger.log(String.format("Running %s with parameters: %s", bot.getClass(),
								Arrays.toString(args)), true);
		run(bot, argList.subList(1, argList.size()));
	}

}

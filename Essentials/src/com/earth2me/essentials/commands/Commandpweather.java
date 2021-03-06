package com.earth2me.essentials.commands;

import static com.earth2me.essentials.I18n._;
import com.earth2me.essentials.User;
import java.util.*;
import org.bukkit.Server;
import org.bukkit.WeatherType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class Commandpweather extends EssentialsCommand
{
	public static final Set<String> getAliases = new HashSet<String>();
	public static final Map<String, WeatherType> weatherAliases = new HashMap<String, WeatherType>();

	static
	{
		getAliases.add("get");
		getAliases.add("list");
		getAliases.add("show");
		getAliases.add("display");
		weatherAliases.put("sun", WeatherType.CLEAR);
		weatherAliases.put("clear", WeatherType.CLEAR);
		weatherAliases.put("storm", WeatherType.DOWNFALL);
		weatherAliases.put("thunder", WeatherType.DOWNFALL);
	}

	public Commandpweather()
	{
		super("pweather");
	}

	@Override
	public void run(final Server server, final CommandSender sender, final String commandLabel, final String[] args) throws Exception
	{
		// Which Players(s) / Users(s) are we interested in?
		String userSelector = null;
		if (args.length == 2)
		{
			userSelector = args[1];
		}
		Set<User> users = getUsers(server, sender, userSelector);

		if (args.length == 0)
		{
			getUsersWeather(sender, users);
			return;
		}

		if (getAliases.contains(args[0]))
		{
			getUsersWeather(sender, users);
			return;
		}

		User user = ess.getUser(sender);
		if (user != null && (!users.contains(user) || users.size() > 1) && !user.isAuthorized("essentials.pweather.others"))
		{
			user.sendMessage(_("pWeatherOthersPermission"));
			return;
		}

		setUsersWeather(sender, users, args[0].toLowerCase());
	}

	/**
	 * Used to get the time and inform
	 */
	private void getUsersWeather(final CommandSender sender, final Collection<User> users)
	{
		if (users.size() > 1)
		{
			sender.sendMessage(_("pWeatherPlayers"));
		}

		for (User user : users)
		{
			if (user.getPlayerWeather() == null)
			{
				sender.sendMessage(_("pWeatherNormal", user.getName()));
			}
			else
			{
				sender.sendMessage(_("pWeatherCurrent", user.getName(), user.getPlayerWeather().toString().toLowerCase(Locale.ENGLISH)));
			}
		}
	}

	/**
	 * Used to set the time and inform of the change
	 */
	private void setUsersWeather(final CommandSender sender, final Collection<User> users, final String weatherType) throws Exception
	{

		final StringBuilder msg = new StringBuilder();
		for (User user : users)
		{
			if (msg.length() > 0)
			{
				msg.append(", ");
			}

			msg.append(user.getName());
		}

		if (weatherType.equalsIgnoreCase("reset"))
		{
			for (User user : users)
			{
				user.resetPlayerWeather();
			}

			sender.sendMessage(_("pWeatherReset", msg));
		}
		else
		{
			if (!weatherAliases.containsKey(weatherType))
			{
				throw new NotEnoughArgumentsException(_("pWeatherInvalidAlias"));
			}

			for (User user : users)
			{
				user.setPlayerWeather(weatherAliases.get(weatherType));
			}
			sender.sendMessage(_("pWeatherSet", weatherType, msg.toString()));
		}
	}

	/**
	 * Used to parse an argument of the type "users(s) selector"
	 */
	private Set<User> getUsers(final Server server, final CommandSender sender, final String selector) throws Exception
	{
		final Set<User> users = new TreeSet<User>(new UserNameComparator());
		// If there is no selector we want the sender itself. Or all users if sender isn't a user.
		if (selector == null)
		{
			final User user = ess.getUser(sender);
			if (user == null)
			{
				for (Player player : server.getOnlinePlayers())
				{
					users.add(ess.getUser(player));
				}
			}
			else
			{
				users.add(user);
			}
			return users;
		}

		// Try to find the user with name = selector
		User user = null;
		final List<Player> matchedPlayers = server.matchPlayer(selector);
		if (!matchedPlayers.isEmpty())
		{
			user = ess.getUser(matchedPlayers.get(0));
		}

		if (user != null)
		{
			users.add(user);
		}
		// If that fails, Is the argument something like "*" or "all"?
		else if (selector.equalsIgnoreCase("*") || selector.equalsIgnoreCase("all"))
		{
			for (Player player : server.getOnlinePlayers())
			{
				users.add(ess.getUser(player));
			}
		}
		// We failed to understand the world target...
		else
		{
			throw new Exception(_("playerNotFound"));
		}

		return users;
	}
}

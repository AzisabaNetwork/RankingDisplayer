package jp.azisaba.lgw.rankingdisplayer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class RankingHideManager {

	private static File file;
	private static YamlConfiguration conf;

	private static final String key = "Players";

	private static List<String> uuidList = new ArrayList<>();

	protected static void init(RankingDisplayer plugin) {
		file = new File(plugin.getDataFolder(), "HideFromRanking.yml");
		conf = YamlConfiguration.loadConfiguration(file);

		if (conf.isSet(key))
			uuidList = new ArrayList<String>(conf.getStringList(key));
	}

	public static boolean isHiding(Player p) {
		return isHiding(p.getUniqueId());
	}

	public static void setHiding(UUID uuid, boolean hide) {
		if (hide && !uuidList.contains(uuid.toString())) {
			uuidList.add(uuid.toString());
		} else if (!hide && uuidList.contains(uuid.toString())) {
			uuidList.remove(uuid.toString());
		}
	}

	public static boolean isHiding(UUID uuid) {
		return uuidList.contains(uuid.toString());
	}

	protected static void save() {
		try {
			conf.set(key, uuidList);
			conf.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

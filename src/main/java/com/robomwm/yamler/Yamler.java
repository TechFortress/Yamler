package com.robomwm.yamler;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Yamler extends JavaPlugin
{
    @Override
    public void onEnable()
    {
        saveConfig();
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                for (Map.Entry<String, YamlConfiguration> changedFile : startYamlin().entrySet())
                {
                    saver(changedFile.getKey(), changedFile.getValue());
                    getLogger().warning("Saved changes to " + changedFile.getKey() + ", restart server to apply.");
                }
            }
        }.runTask(this);
    }

    public void saver(String fileName, YamlConfiguration contents)
    {
        File file = new File(getServer().getWorldContainer() + File.separator + fileName);
        try
        {
            contents.save(file);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public Map<String, YamlConfiguration> startYamlin()
    {
        Map<String, YamlConfiguration> filesToSave = new HashMap<>();

        for (File file : getDataFolder().listFiles())
        {
            YamlConfiguration ours = YamlConfiguration.loadConfiguration(file);
            YamlConfiguration theirs = null;
            switch (file.getName())
            {
                case "bukkit.yml":
                    theirs = getServer().spigot().getBukkitConfig();
                    break;
                case "spigot.yml":
                    theirs = getServer().spigot().getSpigotConfig();
                    break;
                case "paper.yml":
                    theirs = getServer().spigot().getPaperConfig();
                    break;
            }

            if (theirs == null)
                break;

            for (String key : ours.getKeys(true))
            {
                Object originalValue = theirs.get(key);
                Object newValue = ours.get(key);

                if (newValue.equals(originalValue))
                    continue;
                //Ignore configuration sections (we only care about values inside a section)
                if (ours.getConfigurationSection(key) != null)
                    continue;

                getLogger().warning(file.getName() + ": " + key + " was " + originalValue +
                        ", now " + newValue + ".");

                theirs.set(key, newValue);
                filesToSave.put(file.getName(), theirs);
            }
        }

        return filesToSave;
    }
}

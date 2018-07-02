package cc.moecraft.icq.pluginmanager;

import cc.moecraft.icq.PicqBotX;
import cc.moecraft.icq.exceptions.HttpServerStartFailedException;
import cc.moecraft.icq.exceptions.VersionIncorrectException;
import cc.moecraft.icq.pluginmanager.plugin.PluginLoader;
import cc.moecraft.icq.pluginmanager.plugin.PluginManager;
import cc.moecraft.logger.DebugLogger;
import cc.moecraft.yaml.utils.FileUtils;
import com.xiaoleilu.hutool.http.HttpException;
import lombok.Getter;

import java.io.File;
import java.io.IOException;

/**
 * 此类由 Hykilpikonna 在 2018/06/21 创建!
 * Created by Hykilpikonna on 2018/06/21!
 * Github: https://github.com/hykilpikonna
 * QQ: admin@moecraft.cc -OR- 871674895
 *
 * @author Hykilpikonna
 */
public class Launcher
{
    @Getter
    public static LauncherConfig config;

    @Getter
    public static PicqBotX bot;

    @Getter
    public static DebugLogger logger;

    @Getter
    public static PluginManager pluginManager;

    public static void main(String[] args) throws IllegalAccessException, InstantiationException
    {
        initializeConfig();

        bot = new PicqBotX(
                config.getString("ConnectionSettings.PostURL"),
                config.getInt("ConnectionSettings.PostPort"),
                config.getInt("ConnectionSettings.ListeningPort"),
                config.getBoolean("ConnectionSettings.Debug")
        );

        logger = getBot().getLogger();

        if (config.getBoolean("CommandSettings.Enable"))
            bot.enableCommandManager(false, config.getStringList("CommandSettings.Prefixes").toArray(new String[0]));

        // 注册插件
        initializePlugins(bot);

        // TODO: 注册事件

        // TODO: 注册指令

        try
        {
            bot.startBot();
        }
        catch (HttpException e)
        {
            logger.error("HTTP版本验证请求失败.");
            logger.error("可能是因为你没有开酷Q.");
            logger.error("也可能是config.yml配置文件里HTTP发送地址写错了");
        }
        catch (HttpServerStartFailedException | VersionIncorrectException e)
        {
            e.printStackTrace();
        }
    }

    public static boolean initializeConfig()
    {
        config = new LauncherConfig();

        if (!config.getConfigFile().exists())
        {
            try
            {
                config.createFromResources(Launcher.class);
            }
            catch (IOException e)
            {
                logger.error("错误: JAR包已损坏或运行环境错误, 无法找到yml文件");
                return false;
            }
        }

        config.initialize();

        return true;
    }

    public static boolean initializePlugins(PicqBotX bot)
    {
        File pluginRootDir = new File("./plugins/");

        if (!pluginRootDir.isDirectory()) FileUtils.createDir(pluginRootDir.getPath());

        pluginManager = new PluginManager(pluginRootDir, bot);
        pluginManager.enableAllPlugins();

        return false;
    }
}
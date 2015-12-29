package ru.alastar;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.bukkit.BukkitPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import lib.PatPeter.SQLibrary.MySQL;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import static com.sk89q.worldguard.protection.flags.StateFlag.State.ALLOW;
import static com.sk89q.worldguard.protection.flags.StateFlag.State.DENY;

/**
 * Created by Alastar on 24.06.2015.
 */
public class Main extends JavaPlugin implements Listener {

    protected Logger log;
    protected FileConfiguration config;

    protected boolean useWorldGuard = false;
    protected WorldGuardPlugin wg_plugin = null;
    protected WorldEdit we_plugin = null;

    protected boolean useSQL = false;
    protected MySQL mysql = null;

    protected BinaryManager binManager = null;


    public boolean useKey = false;
    public int keyId = 373;
    protected boolean useEconomy = false;
    protected Permission permission = null;
    protected Economy economy = null;
    protected Chat chat = null;
    protected int cost = 100;

    public HashMap<String, Housing> housings; // Key is name of house

    protected boolean limitHousings = false;
    protected int limit = 1;

    protected int _cell_width = 50;

    protected World _housing_world = null;
    private int housesPerRow = 50;
    private double platformY = 50;
    private double platformWidth = 5;
    private double regionHeight = 255;

    private ArrayList<Invite> pendings = new ArrayList<>();
    private ArrayList<InviteClick> inviters = new ArrayList<>();
    private FileConfiguration lang = null;
    private String specify_command;

    private List<String> flags_allow;
    private List<String> flags_deny;
    private List<String> restrict_invite;
    private String cant_invite;
    private String htele_alias = "htele";
    private String haccept_alias = "haccept";
    private String hdecline_alias = "hdecline";
    private String hcreate_alias = "hcreate";
    private String hlist_alias = "hlist";
    private String hsetspawn_alias = "hsetspawn";
    private String hexport_alias = "hexport";
    private String hinvite_alias = "hinvite";
    private String cant_or_no_perm = "You can't do this, or just have no permissions!";
    private String htele_perm = "HAB.tele";
    private String hcreate_perm = "HAB.create";
    private String hinvite_perm = "HAB.invite";
    private String haccept_perm = "HAB.accept";
    private String hdecline_perm = "HAB.decline";
    private String hlist_perm = "HAB.list";
    private String hexport_perm = "HAB.export";
    private String cmd_list = "List of commands:";
    private String hsetspawn_perm = "HAB.setspawn";
    private String usage;
    private String htele_desc;
    private String htele_args;
    private String haccept_desc;
    private String hdecline_desc;
    private String hcreate_args;
    private String hcreate_desc;
    private String hlist_desc;
    private String hsetspawn_desc;
    private String hexport_desc;
    private String hexport_args;
    private String hinvite_desc;
    private String desc;
    private String hsetspawn_args;
    private static String key_give;
    private String key_info;
    private boolean allow_stealing = false;
    private boolean passOnlyByKey = false;

    public void onEnable() {
        log = getLogger();
        config = getConfig();
        housings = new HashMap<>();
        useWorldGuard = config.getBoolean("use_world_guard");
        useSQL = config.getBoolean("use_sql");
        useEconomy = config.getBoolean("use_economy");
        limitHousings = config.getBoolean("limit_houses_per_player");
        _cell_width = config.getInt("house_max_width");
        housesPerRow = config.getInt("houses_per_row");
        platformY = config.getInt("platform_y");
        platformWidth = config.getInt("platform_width");
        String lang_file = config.getString("lang");
        allow_stealing = config.getBoolean("allow_stealing");
        passOnlyByKey = config.getBoolean("passOnlyByKey");

        useKey = config.getBoolean("passByKey");
        keyId = config.getInt("keyId");
        flags_allow = config.getStringList("flags_allow");
        flags_deny = config.getStringList("flags_deny");
        restrict_invite = config.getStringList("restrict_invite");

        log.info("Custom flags loaded:");
        log.info("ALLOW:");

        for (final String allow : flags_allow) {
            log.info(allow);
        }
        log.info("DENY:");
        for (final String deny : flags_deny) {
            log.info(deny);
        }

        log.info("Entities restricted:");

        for (final String restricted : restrict_invite) {
            log.info(restricted);
        }

        if (limitHousings) {
            limit = config.getInt("house_limit");
        }

        lang = new YamlConfiguration();
        try {
            lang.load(System.getProperty("user.dir") + "/plugins/HouseInaBottle/" + lang_file + ".yml");
            loadLanguage();

        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            this.setEnabled(false);
        }

        if (useSQL) {
            if (getPlugin(lib.PatPeter.SQLibrary.SQLibrary.class) != null) {
                mysql = new MySQL(Logger.getLogger("Minecraft"), "HAB", config.getString("db_host"), config.getInt("db_port"), config.getString("db_name"), config.getString("db_user"), config.getString("db_password") + "");
                if (mysql.open()) {
                    loadHousings();
                } else {
                    log.info("SQL connection is failed! Aborting!");
                    this.setEnabled(false);
                    return;
                }
            } else {
                log.info("SQLLibrary not found! Aborting!");
                this.setEnabled(false);
                return;
            }
        } else {
            initBinaryManager();
        }

        if (useWorldGuard) {
            regionHeight = config.getInt("region_height");
            if (getPlugin(com.sk89q.worldguard.bukkit.WorldGuardPlugin.class) != null) {
                wg_plugin = WorldGuardPlugin.inst();
            } else {
                log.info("World Guard plugin not found! Aborting!");
                this.setEnabled(false);
                return;
            }
        }

        if (getPlugin(com.sk89q.worldedit.bukkit.WorldEditPlugin.class) != null) {
            we_plugin = WorldEdit.getInstance();
        } else {
            log.info("World Edit plugin not found! Aborting!");
            this.setEnabled(false);
            return;
        }

        if (getPlugin(net.milkbowl.vault.Vault.class) != null) {
            setupPermissions();
            setupChat();
        } else {
            log.info("Vault plugin not found! Aborting!");
            this.setEnabled(false);
            return;
        }

        if (useEconomy) {
            cost = config.getInt("housing_cost");
            if (getPlugin(net.milkbowl.vault.Vault.class) != null) {
                setupEconomy();
            } else {
                log.info("Vault plugin not found! Aborting!");
                this.setEnabled(false);
                return;
            }
        }
        getServer().getPluginManager().registerEvents(this, this);
        tryLoadWorld();
    }

    private void loadLanguage() {
        not_owned = lang.getString("not_owned");
        welcome = lang.getString("welcome");
        specify_house_name = lang.getString("specify_house_name");
        right_click = lang.getString("right_click");
        cant_invite_to_others = lang.getString("cant_invite_to_others");
        accepted_invite = lang.getString("accepted_invite");
        refused_invite = lang.getString("refused_invite");
        invited_you = lang.getString("invited_you");
        specify_target = lang.getString("specify_target");
        specify_command = lang.getString("specify_command");
        list = lang.getString("list");
        setspawn = lang.getString("setspawn");
        cant_setspawn = lang.getString("cant_setspawn");
        wrong_dim = lang.getString("wrong_dim");
        wrong_housing = lang.getString("wrong_housing");
        lack_of_money = lang.getString("lack_of_money");
        too_many_houses = lang.getString("too_many_houses");
        house_exists = lang.getString("house_exists");
        new_house = lang.getString("new_house");
        cant_invite = lang.getString("cant_invite");

        htele_alias = lang.getString("htele");
        htele_perm = lang.getString("htele_perm");
        htele_desc = lang.getString("htele_desc");
        htele_args = lang.getString("htele_args");

        haccept_alias = lang.getString("haccept");
        haccept_perm = lang.getString("haccept_perm");
        haccept_desc = lang.getString("haccept_desc");

        hdecline_alias = lang.getString("hdecline");
        hdecline_perm = lang.getString("hdecline_perm");
        hdecline_desc = lang.getString("hdecline_desc");

        hcreate_alias = lang.getString("hcreate");
        hcreate_perm = lang.getString("hcreate_perm");
        hcreate_desc = lang.getString("hcreate_desc");
        hcreate_args = lang.getString("hcreate_args");

        hlist_alias = lang.getString("hlist");
        hlist_perm = lang.getString("hlist_perm");
        hlist_desc = lang.getString("hlist_desc");

        hsetspawn_alias = lang.getString("hsetspawn");
        hsetspawn_perm = lang.getString("hsetspawn_perm");
        hsetspawn_desc = lang.getString("hsetspawn_desc");
        hsetspawn_args = lang.getString("hsetspawn_args");

        hexport_alias = lang.getString("hexport");
        hexport_perm = lang.getString("hexport_perm");
        hexport_desc = lang.getString("hexport_desc");
        hexport_args = lang.getString("hexport_args");

        hinvite_alias = lang.getString("hinvite");
        hinvite_perm = lang.getString("hinvite_perm");
        hinvite_desc = lang.getString("hinvite_desc");

        cmd_list = lang.getString("cmd_list");
        usage = lang.getString("usage");
        desc = lang.getString("desc");
        key_give = lang.getString("key_give");
        key_info = lang.getString("key_info");
    }

    private void loadHousings() {
        PreparedStatement ps;
        housings = new HashMap<>();

        try {
            ps = mysql.prepare("SELECT * FROM " + config.getString("db_table") + ";");
            ResultSet set = mysql.query(ps);
            while (set.next()) {
                housings.put(set.getString("name"), new Housing(set.getDouble("x_tele"), set.getDouble("y_tele"), set.getDouble("z_tele"), set.getInt("x_grid"), set.getInt("y_grid"), set.getString("player_name")));
            }
            mysql.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
        if (chatProvider != null) {
            chat = chatProvider.getProvider();
        }

        return (chat != null);
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }

    private void tryLoadWorld() {
        _housing_world = getServer().getWorld("housing");
        if (_housing_world == null) {
            _housing_world = getServer().createWorld(new HousingWorldCreator("housing"));
            _housing_world.setAmbientSpawnLimit(0);
            _housing_world.setAnimalSpawnLimit(0);
            _housing_world.setMonsterSpawnLimit(0);
            _housing_world.setKeepSpawnInMemory(false);
        }
    }

    private void initBinaryManager() {
        binManager = new BinaryManager();
        housings = new HashMap<>();
        HashMap<String, Housing> loaded = binManager.loadDB();
        if (loaded != null) {
            housings.putAll(loaded);
        }
    }

    public void onDisable() {
        saveConfig();
        if (!useSQL) {
            binManager.saveDB(housings);
        } else {
            try {
                if (saveSQL(housings)) {
                    log.info("Data saved successfully!");
                } else {
                    log.info("SQLDatabase is unreachable, dumping saves...");
                    binManager.dumpDB(housings);
                    log.info("...done!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static String welcome;
    private static String not_owned;
    private static String specify_house_name;
    private static String right_click;
    private static String cant_invite_to_others;
    private static String accepted_invite;
    private static String refused_invite;
    private static String invited_you;
    private static String specify_target;
    private static String list;
    private static String setspawn;
    private static String cant_setspawn;
    private static String wrong_dim;
    private static String wrong_housing;
    private static String lack_of_money;
    private static String too_many_houses;
    private static String house_exists;
    private static String new_house;


    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (cmd.getName().equalsIgnoreCase("habhelp")) {
            buildHelpMsg(sender);
        }
        if (cmd.getName().equalsIgnoreCase(htele_alias.toLowerCase())) {
            if (permission.has(sender, htele_perm)) {
                if (!passOnlyByKey || (passOnlyByKey && !useKey)) {
                    if (args.length != 1) {
                        return false;
                    } else {
                        String houseName = args[0];
                        Housing house = getHousing(houseName);
                        if (house != null) {
                            if (house.checkOwner((Player) sender)) {
                                Location loc = new Location(_housing_world, house._x_tel, house._y_tel, house._z_tel);
                                ((Player) sender).teleport(loc);
                                sender.sendMessage(welcome);
                            } else {
                                sender.sendMessage(not_owned);
                            }
                        } else {
                            sender.sendMessage(wrong_housing);
                        }
                    }
                    return true;
                } else {
                    sender.sendMessage("...");
                }
            } else {
                sender.sendMessage(cant_or_no_perm);
            }
        }

        if (cmd.getName().equalsIgnoreCase(hcreate_alias)) {
            if (permission.has(sender, hcreate_perm)) {
                if (args.length == 1 && sender instanceof Player) {
                    tryCreateFor((Player) sender, args[0]);
                } else {
                    assert sender != null;
                    sender.sendMessage(specify_house_name);
                }
                return true;
            } else {
                sender.sendMessage(cant_or_no_perm);
            }
        } else if (cmd.getName().equalsIgnoreCase(hsetspawn_alias)) {
            if (permission.has(sender, hsetspawn_perm)) {
                if (args.length == 1 && sender instanceof Player) {
                    String hName = args[0];
                    tryRelocateSpawn(hName, (Player) sender);
                } else {
                    assert sender != null;
                    sender.sendMessage(specify_house_name);
                }
                return true;
            } else {
                sender.sendMessage(cant_or_no_perm);
            }
        } else if (cmd.getName().equalsIgnoreCase(hinvite_alias.toLowerCase())) {
            if (args.length == 1 && sender != null) {
                if (permission.has(sender, hinvite_perm)) {
                    if (sender instanceof Player) {
                        if (getHousing(args[0]) != null) {
                            if (getHousing(args[0]).checkOwner((Player) sender) || (allow_stealing && hasKey((Player) sender, args[0]))) {
                                inviters.add(new InviteClick((Player) sender, args[0]));
                                sender.sendMessage(right_click);
                            } else {
                                sender.sendMessage(cant_invite_to_others);
                            }
                        } else {
                            sender.sendMessage(wrong_housing);
                        }
                    }
                } else {
                    sender.sendMessage(cant_or_no_perm);
                }
            } else {
                assert sender != null;
                sender.sendMessage(specify_house_name);
            }
            return true;
        } else if (cmd.getName().

                equalsIgnoreCase(haccept_alias.toLowerCase()

                ))

        {
            if (sender != null) {
                if (permission.has(sender, haccept_perm)) {

                    if (sender instanceof Player) {
                        Invite invite = getInvite((Player) sender);
                        if (invite != null) {
                            invite.inviter.sendMessage("" + accepted_invite.replace("<player>", invite.invited.getName()));

                            teleportToHouse(invite.invited, invite.to);
                            pendings.remove(invite);
                        }
                    }
                } else {
                    sender.sendMessage(cant_or_no_perm);
                }
            }
            return true;
        } else if (cmd.getName().

                equalsIgnoreCase(hdecline_alias.toLowerCase()

                ))

        {
            if (sender != null) {
                if (permission.has(sender, hdecline_perm)) {

                    if (sender instanceof Player) {
                        Invite invite = getInvite((Player) sender);
                        if (invite != null) {
                            invite.inviter.sendMessage("" + refused_invite.replace("<player>", invite.invited.getName()));
                            pendings.remove(invite);
                        }
                    }
                } else {
                    sender.sendMessage(cant_or_no_perm);
                }
            }
            return true;
        } else if (cmd.getName().equalsIgnoreCase(hlist_alias.toLowerCase())) {
            if (sender != null) {
                if (permission.has(sender, hlist_perm)) {
                    if (sender instanceof Player) {
                        if (useKey && allow_stealing) {
                            sender.sendMessage("...");
                        } else
                            listHousings(sender);
                    }

                } else {
                    sender.sendMessage(cant_or_no_perm);
                }
            }
            return true;
        } else if (cmd.getName().

                equalsIgnoreCase(hexport_alias.toLowerCase()

                ))

        {
            if (permission.has(sender, hexport_perm)) {
                if (args.length == 1) {
                    String to = args[0];
                    tryExport(to, sender);

                } else if (sender != null) {
                    sender.sendMessage(specify_target);
                }
                return true;
            } else {
                sender.sendMessage(cant_or_no_perm);
            }
        }

        return false;
    }

    private boolean hasKey(Player sender, String arg) {
        PlayerInventory inv = sender.getInventory();
        for(ItemStack stack: inv.getContents()){
            if(stack != null) {
                if(stack.getItemMeta() != null) {
                    if (stack.getItemMeta().getLore() != null) {
                        if(stack.getItemMeta().getLore().size() > 0)
                        {
                            if(stack.getItemMeta().getLore().get(0) == "House In A Bottle"){
                                if(stack.getItemMeta().getLore().get(1) == arg)
                                {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private void buildHelpMsg(CommandSender sender) {
        sender.sendMessage(cmd_list);

        //htele
        sender.sendMessage(htele_alias);
        sender.sendMessage(desc + htele_desc);
        sender.sendMessage("Permission: " + htele_perm);
        sender.sendMessage(usage + "/" + htele_alias + htele_args);

        //hsetspawn
        sender.sendMessage(hsetspawn_alias);
        sender.sendMessage(desc + hsetspawn_desc);
        sender.sendMessage("Permission: " + hsetspawn_perm);
        sender.sendMessage(usage + "/" + hsetspawn_alias + hsetspawn_args);

        //hcreate
        sender.sendMessage(hcreate_alias);

        sender.sendMessage(desc + hcreate_desc);

        sender.sendMessage("Permission: " + hcreate_perm);
        sender.sendMessage(usage + "/" + hcreate_alias + hcreate_args);


        //hexport
        sender.sendMessage(hexport_alias);

        sender.sendMessage(desc + hexport_desc);

        sender.sendMessage("Permission: " + hexport_perm);
        sender.sendMessage(usage + "/" + hexport_alias + hexport_args);


        //hlist
        sender.sendMessage(hlist_alias);

        sender.sendMessage(desc + hlist_desc);

        sender.sendMessage("Permission: " + hlist_perm);
        sender.sendMessage(usage + "/" + hlist_alias);


        //hinvite
        sender.sendMessage(hinvite_alias);

        sender.sendMessage(desc + hinvite_desc);

        sender.sendMessage("Permission: " + hinvite_perm);
        sender.sendMessage(usage + "/" + hinvite_alias);


        //haccept
        sender.sendMessage(haccept_alias);

        sender.sendMessage(desc + haccept_desc);

        sender.sendMessage("Permission: " + haccept_perm);
        sender.sendMessage(usage + "/" + haccept_alias);


        //hdecline
        sender.sendMessage(hdecline_alias);

        sender.sendMessage(desc + hdecline_desc);

        sender.sendMessage("Permission: " + hdecline_perm);
        sender.sendMessage(usage + "/" + hdecline_alias);


    }

    private void tryExport(String to, CommandSender sender) {
        if (to.equalsIgnoreCase("sql")) {
            HashMap<String, Housing> dump;
            dump = new BinaryManager().loadDB();
            if (dump != null) {
                try {
                    saveSQL(dump);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                final String msg = dump.size() + " housings was exported to MySQL database!";
                log.info(msg);
                assert sender != null;
                sender.sendMessage(msg);
            }
        } else if (to.equalsIgnoreCase("bin")) {
            PreparedStatement ps;
            HashMap<String, Housing> dump = new HashMap<>();

            try {
                if (mysql.open()) {
                    ps = mysql.prepare("SELECT * FROM " + config.getString("db_table") + ";");
                    ResultSet set = mysql.query(ps);
                    while (set.next()) {
                        dump.put(set.getString("name"), new Housing(set.getDouble("x_tele"), set.getDouble("y_tele"), set.getDouble("z_tele"), set.getInt("x_grid"), set.getInt("y_grid"), set.getString("player_name")));
                    }
                    mysql.close();
                } else {
                    log.info("MySQL connection failed!");

                    assert sender != null;
                    sender.sendMessage("MySQL connection failed!");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            new BinaryManager().saveDB(dump);
            final String msg = dump.size() + " housings was exported to binary database!";
            log.info(msg);

            assert sender != null;
            sender.sendMessage(msg);
        }
    }

    private boolean saveSQL(HashMap<String, Housing> toSave) throws SQLException {
        if (mysql.open()) {
            PreparedStatement ps;
            ResultSet set;
            Housing house;
            for (final String name : toSave.keySet()) {
                ps = mysql.prepare("SELECT * FROM " + config.getString("db_table") + " WHERE name='" + name + "';");
                set = mysql.query(ps);
                house = toSave.get(name);

                if (!set.next()) {
                    ps = mysql.prepare("INSERT INTO " + config.getString("db_table") + "(name, x_tele, y_tele, z_tele, x_grid, y_grid, player_name) VALUES('" + name + "', " + house._x_tel + "," + house._y_tel + "," + house._z_tel + "," + house._x_grid + "," + house._y_grid + ",'" + house._name + "');");
                    mysql.query(ps);
                } else {
                    ps = mysql.prepare("UPDATE " + config.getString("db_table") + " SET x_tele=" + house._x_tel + ", y_tele=" + house._y_tel + ", z_tele=" + house._z_tel + " WHERE name='" + name + "';");
                    mysql.query(ps);
                }
            }
            mysql.close();
            return true;
        }
        return false;
    }

    private void listHousings(CommandSender sender) {
        if (sender instanceof Player) {
            sender.sendMessage(list);
            for (final String name : housings.keySet()) {
                if (housings.get(name).checkOwner((Player) sender)) {
                    sender.sendMessage(name);
                }
            }
        }
    }

    private Invite getInvite(Player sender) {
        for (final Invite inv : pendings) {
            if (inv.invited.equals(sender))
                return inv;
        }
        return null;
    }

    @EventHandler
    public void onItemClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        // log.info("Interact!");
        if (player.getItemInHand() != null) {
            //    log.info("Has an item in hand!");
            if (player.getItemInHand().getItemMeta().getLore() != null) {
                if (player.getItemInHand().getItemMeta().getLore().size() > 0 && player.getItemInHand().getItemMeta().getLore().get(0).equals("House In A Bottle")) {
                    //           log.info("Tis is a key!!");
                    String house_name = player.getItemInHand().getItemMeta().getLore().get(1);
                    Housing house = housings.get(house_name);
                    if (house != null) {
                        if (house.checkOwner((Player) player) || allow_stealing) {
                            Location loc = new Location(_housing_world, house._x_tel, house._y_tel, house._z_tel);
                            ((Player) player).teleport(loc);
                            player.sendMessage(welcome);
                        } else {
                            player.sendMessage(not_owned);
                        }
                    } else {
                        player.sendMessage(wrong_housing);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        if (inviting(event.getPlayer())) {
            //log.info( "interactPlayer");
            Entity en = event.getRightClicked();
            if (!restrict_invite.contains(en.getName().toLowerCase())) {
                if (en instanceof Player) {
                    InviteClick click = getClick(event.getPlayer());
                    if (click != null) {
                        Invite invitation = new Invite(event.getPlayer(), (Player) event.getRightClicked(), click.housingName);
                        pendings.add(invitation);
                        inviters.remove(getClick(event.getPlayer()));
                        event.getRightClicked().sendMessage(invited_you.replace("<player>", event.getPlayer().getName()) + " ");
                    }
                } else if (en instanceof Creature) {
                    //  log.info( "target is creature");
                    InviteClick click = getClick(event.getPlayer());
                    if (click != null) {
                        teleportToHouse(event.getRightClicked(), click.housingName);
                        inviters.remove(getClick(event.getPlayer()));
                    }
                }
            } else
                event.getPlayer().sendMessage(cant_invite);
        }
    }

    private InviteClick getClick(Player player) {
        for (final InviteClick inviteClick : inviters) {
            if (inviteClick.sender.equals(player))
                return inviteClick;
        }
        return null;
    }

    private boolean inviting(Player player) {
        for (final InviteClick inviteClick : inviters) {
            if (inviteClick.sender.equals(player))
                return true;
        }
        return false;
    }

    private void teleportToHouse(Entity rightClicked, String to) {
        Housing house = getHousing(to);
        Location loc = new Location(_housing_world, house._x_tel, house._y_tel, house._z_tel);
        rightClicked.teleport(loc);
    }

    private void tryRelocateSpawn(String hName, Player sender) {
        Housing house = getHousing(hName);
        if (house != null) {
            if (house.checkOwner(sender) || allow_stealing) {
                if (sender.getWorld().equals(_housing_world)) {
                    if (isInHouse(sender, hName)) {
                        house._x_tel = sender.getLocation().getBlockX();
                        house._y_tel = sender.getLocation().getBlockY();
                        house._z_tel = sender.getLocation().getBlockZ();
                        sender.sendMessage(setspawn.replace("<point>", house._x_tel + " " + house._y_tel + " " + house._z_tel));
                    } else {
                        sender.sendMessage(cant_setspawn);
                    }
                } else {
                    sender.sendMessage(wrong_dim);
                }
            } else {
                sender.sendMessage(not_owned);
            }
        } else {
            sender.sendMessage(wrong_housing);
        }
    }

    private boolean isInHouse(final Player sender, final String hName) {
        Housing house = getHousing(hName);
        if (house != null) {
            final double xleft = house._x_grid * _cell_width;
            final double ybot = house._y_grid * _cell_width;

            final double xright = xleft + _cell_width;
            final double ytop = ybot + _cell_width;

            final double xP = sender.getLocation().getBlockX();
            final double yP = sender.getLocation().getBlockZ();

            if (xP >= xleft && xP <= xright && yP >= ybot && yP <= ytop) {
                return true;
            }
        }
        return false;
    }

    private Housing getHousing(String houseName) {
        return housings.get(houseName);
    }

    private void tryCreateFor(Player sender, String hName) {
        if (!housings.containsKey(hName)) {
            int numOfHouses = 0;
            for (final String name : housings.keySet()) {
                if (housings.get(name).checkOwner(sender)) {
                    ++numOfHouses;
                }
            }
            if (numOfHouses < limit || !limitHousings) {
                if (useEconomy && economy != null) {
                    if (economy.has(sender, cost)) {
                        economy.withdrawPlayer(sender, cost);
                        createHouse(sender, hName);
                    } else {
                        sender.sendMessage(lack_of_money);
                    }
                } else {
                    createHouse(sender, hName);
                }
            } else {
                sender.sendMessage(too_many_houses);
            }
        } else {
            sender.sendMessage(house_exists);
        }
    }


    private void createHouse(Player sender, String houseName) {
        int housesCount = housings.keySet().size();
        int y_rows = (int) Math.floor(housesCount / housesPerRow);
        int x_rows = housesCount - (y_rows * housesPerRow);
        if (x_rows < housesPerRow) {
            ++x_rows;
        } else {
            x_rows = 0;
            ++y_rows;
        }

        double x_coords = x_rows * _cell_width;
        double y_coords = y_rows * _cell_width;

        double x_center = x_coords + _cell_width / 2;
        double y_center = y_coords + _cell_width / 2;

        Housing new_house = new Housing(x_center, platformY + 3, y_center, x_rows, y_rows, sender.getName());

        housings.put(houseName, new_house);
        com.sk89q.worldedit.Vector leftBot = new com.sk89q.worldedit.Vector(x_center - platformWidth, platformY, y_center - platformWidth);
        com.sk89q.worldedit.Vector rightUp = new com.sk89q.worldedit.Vector(x_center + platformWidth, platformY, y_center + platformWidth);
        CuboidRegion cube = new CuboidRegion(leftBot, rightUp);

        EditSession session = new EditSession(new BukkitWorld(_housing_world), cube.getArea());
        try {
            session.setBlocks(cube, new BaseBlock(1));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (useWorldGuard) {
            BlockVector p1 = new BlockVector(new com.sk89q.worldedit.Vector(x_coords, 0, y_coords));
            BlockVector p2 = new BlockVector(new com.sk89q.worldedit.Vector(x_coords + _cell_width, regionHeight, y_coords + _cell_width));
            ProtectedRegion region = new ProtectedCuboidRegion(houseName + "-house", p1, p2);
            wg_plugin.getRegionManager(_housing_world).addRegion(region);
            DefaultDomain owners = new DefaultDomain();
            owners.addPlayer(new BukkitPlayer(wg_plugin, sender));
            region.setOwners(owners);
            Flag[] var1 = DefaultFlag.getFlags();
            int var2 = var1.length;
            for (final String allow : flags_allow) {
                for (int var3 = 0; var3 < var2; ++var3) {
                    Flag flag = var1[var3];
                    if (flag.getName().replace("-", "").equalsIgnoreCase(allow.replace("-", ""))) {
                        region.setFlag(flag, ALLOW);
                    }
                }
            }
            for (final String deny : flags_deny) {
                for (int var3 = 0; var3 < var2; ++var3) {
                    Flag flag = var1[var3];
                    if (flag.getName().replace("-", "").equalsIgnoreCase(deny.replace("-", ""))) {
                        region.setFlag(flag, DENY);
                    }
                }
            }

        }
        sender.sendMessage(Main.new_house.replace("<name>", houseName));
        if (useKey) {
            giveKey(sender, houseName);
        }
    }

    private void giveKey(Player to, String house) {
        ItemStack keyItem = new ItemStack(Material.getMaterial(keyId));
        ItemMeta meta = keyItem.getItemMeta();

        meta.setDisplayName(key_info.replace("<name>", house));
        List<String> lore = new ArrayList<String>();
        lore.add("House In A Bottle");
        lore.add(house);
        meta.setLore(lore);
        keyItem.setItemMeta(meta);
        to.getLocation().getWorld().dropItem(to.getLocation(), keyItem);
        to.sendMessage(Main.key_give.replace("<name>", house));
    }
}
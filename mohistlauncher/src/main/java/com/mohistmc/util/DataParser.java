/*
 * MohistMC
 * Copyright (C) 2018-2023.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mohistmc.util;

import com.mohistmc.MohistMCStart;
import com.mohistmc.tools.FileUtils;
import com.mohistmc.tools.OSUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Shawiiz_z
 * @version 0.1
 * @date 09/05/2022 22:05
 */
public class DataParser {
    public static HashMap<String, String> versionMap = new HashMap<>();
    public static List<String> launchArgs = new ArrayList<>();

    public static void parseVersions() {
        versionMap.put("forge", FileUtils.readFileFromJar(DataParser.class.getClassLoader(), "versions/forge.txt").get(0));
        versionMap.put("minecraft", FileUtils.readFileFromJar(DataParser.class.getClassLoader(), "versions/minecraft.txt").get(0));
        versionMap.put("mcp", FileUtils.readFileFromJar(DataParser.class.getClassLoader(), "versions/mcp.txt").get(0));
        versionMap.put("mohist",FileUtils.readFileFromJar(DataParser.class.getClassLoader(), "versions/mohist.txt").get(0));

        MohistMCStart.MCVERSION = versionMap.get("minecraft");
    }

    public static void parseLaunchArgs() {
        launchArgs.addAll(FileUtils.readFileFromJar(DataParser.class.getClassLoader(), "data/" + (OSUtil.getOS().equals(OSUtil.OS.WINDOWS) ? "win" : "unix") + "_args.txt"));
    }
}

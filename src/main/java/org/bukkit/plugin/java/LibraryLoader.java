package org.bukkit.plugin.java;

import com.mohistmc.MohistMC;
import com.mohistmc.bukkit.PluginsLibrarySource;
import com.mohistmc.bukkit.remapping.RemappingURLClassLoader;
import com.mohistmc.mjson.Json;
import com.mohistmc.tools.ConnectionUtil;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

class LibraryLoader {

    public LibraryLoader() {
    }

    @Nullable
    public ClassLoader createLoader(@NotNull PluginDescriptionFile desc) {
        if (desc.getLibraries().isEmpty()) {
            return null;
        }
        MohistMC.LOGGER.info("[{}] Loading {} libraries... please wait", desc.getName(), desc.getLibraries().size());

        List<Dependency> dependencies = new ArrayList<>();
        for (String libraries : desc.getLibraries()) {
            String[] args = libraries.split(":");
            if (args.length > 1) {
                Dependency dependency = new Dependency(args[0], args[1], args[2], false);
                dependencies.add(dependency);
            }
        }

        Set<File> libraries = new HashSet<>();
        Set<Dependency> newDependencies = new HashSet<>();
        var d = mohistLibs();

        for (Dependency dependency : dependencies) {
            String group = dependency.group().replace(".", "/");
            String fileName = "%s-%s.jar".formatted(dependency.name(), dependency.version());
            if (!d.contains(fileName)) {
                if (dependency.version().equalsIgnoreCase("LATEST")) {
                    newDependencies.add(findDependency(group, dependency.name(), false));
                } else {
                    newDependencies.add(dependency);
                    String pomUrl = PluginsLibrarySource.DEFAULT + "%s/%s/%s/%s".formatted(group, dependency.name(), dependency.version(), fileName.replace("jar", "pom"));
                    if (ConnectionUtil.isValid(pomUrl)) {
                        newDependencies.addAll(initDependencies0(pomUrl));
                    }
                }
            }
        }

        MohistMC.LOGGER.info("[{}] Loading {} extra libraries... please wait", desc.getName(), newDependencies.size() - desc.getLibraries().size());

        for (Dependency dependency : newDependencies) {
            String group = dependency.group().replace(".", "/");
            String fileName = "%s-%s.jar".formatted(dependency.name(), dependency.version());
            String mavenUrl = PluginsLibrarySource.DEFAULT + "%s/%s/%s/%s".formatted(group, dependency.name(), dependency.version(), fileName);

            File file = new File(new File("libraries", "plugins-lib"), "%s/%s/%s/%s".formatted(group, dependency.name(), dependency.version(), fileName));

            if (file.exists()) {
                MohistMC.LOGGER.info("[{}] Found libraries {}", desc.getName(), file);
                libraries.add(file);
                continue;
            }
            try {
                file.getParentFile().mkdirs();

                InputStream inputStream = new URL(mavenUrl).openStream();
                ReadableByteChannel rbc = Channels.newChannel(inputStream);
                FileChannel fc = FileChannel.open(file.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);

                fc.transferFrom(rbc, 0, Long.MAX_VALUE);
                fc.close();
                rbc.close();

                libraries.add(file);
            } catch (IOException ignored) {
            }
        }

        List<URL> jarFiles = new ArrayList<>();
        for (File file : libraries) {
            try {
                jarFiles.add(file.toURI().toURL());
                MohistMC.LOGGER.info("[{}] Loaded libraries {}", desc.getName(), file);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        return new RemappingURLClassLoader(jarFiles.toArray(new URL[0]), getClass().getClassLoader());
    }

    public Set<Dependency> initDependencies0(String url) {
        Set<Dependency> list = new HashSet<>();
        for (Dependency dependency : initDependencies(url)) {
            list.add(dependency);
            if (dependency.extra()) {
                String group = dependency.group().replace(".", "/");
                String fileName = "%s-%s.jar".formatted(dependency.name(), dependency.version());
                String pomUrl = PluginsLibrarySource.DEFAULT + "%s/%s/%s/%s".formatted(group, dependency.name(), dependency.version(), fileName.replace("jar", "pom"));
                if (ConnectionUtil.isValid(pomUrl)) list.addAll(initDependencies(pomUrl));
            }
        }
        return list;
    }

    public Set<Dependency> initDependencies(String url) {
        Set<Dependency> list = new HashSet<>();
        Json json2Json = Json.readXml(url).at("project");
        String version = json2Json.has("parent") ? json2Json.at("parent").asString("version") : json2Json.asString("version");
        String groupId = json2Json.has("parent") ? json2Json.at("parent").asString("groupId") : json2Json.asString("groupId");

        if (!json2Json.has("dependencies")) return list;
        if (!json2Json.at("dependencies").toString().startsWith("{\"dependency\"")) return list;
        Json json3Json = json2Json.at("dependencies").at("dependency");
        if (json3Json.isArray()) {
            for (Json o : json2Json.at("dependencies").asJsonList("dependency")) {
                dependency(o, list, version, groupId);
            }
        } else {
            dependency(json3Json, list, version, groupId);
        }
        return list;
    }

    public void dependency(Json json, Set<Dependency> list, String version, String parent_groupId) {
        try {
            if (json.toString().contains("groupId") && json.toString().contains("artifactId")) {
                String groupId = json.asString("groupId");
                String artifactId = json.asString("artifactId");
                if (json.has("optional")) {
                    return;
                }
                if (json.toString().contains("version")) {
                    if (json.has("scope") && (json.asString("scope").equals("test")) || json.asString("scope").equals("provided")) {
                        return;
                    }
                    if (groupId.equals("${project.parent.groupId}")) {
                        groupId = parent_groupId;
                    }
                    String versionAsString = json.asString("version");
                    if (versionAsString.contains("${project.version}") || versionAsString.contains("${project.parent.version}")) {
                        Dependency dependency = new Dependency(groupId, artifactId, version, true);
                        list.add(dependency);
                    } else if (!versionAsString.contains("${")) {
                        Dependency dependency = new Dependency(groupId, artifactId, versionAsString, true);
                        list.add(dependency);
                    }
                } else {
                    list.add(findDependency(groupId, artifactId, true));
                }
            }
        } catch (Exception ignored) {}
    }

    public static Dependency findDependency(String groupId, String artifactId, boolean extra) {
        String mavenUrl = "https://repo.maven.apache.org/maven2/%s/%s/%s".formatted(groupId.replace(".", "/"), artifactId, "maven-metadata.xml");
        Json compile_json2Json = Json.readXml(mavenUrl).at("metadata");
        List<Object> v = compile_json2Json.at("versioning").at("versions").at("version").asList();
        return new Dependency(groupId, artifactId, (String) v.get(v.size() - 1), extra);
    }

    public List<String> mohistLibs() {
        List<String> temp = new ArrayList<>();
        BufferedReader b = new BufferedReader(new InputStreamReader(LibraryLoader.class.getClassLoader().getResourceAsStream("libraries.txt")));
        String str;
        try {
            while ((str = b.readLine()) != null) {
                String[] s = str.split("\\|");
                temp.add(new File("libraries", s[0]).getName());
            }
            b.close();
        } catch (Exception ignored) {}
        return temp;
    }

    public record Dependency(String group, String name, String version, boolean extra) {
    }
}

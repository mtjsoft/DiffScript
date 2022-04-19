package cn.mtjsoft;

import cn.mtjsoft.view.AutoWindow;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import javax.swing.*;
import java.util.List;

public class AutoMain {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Namespace ns = start(args);
        String diffInput = "";
        String oldApkPath = "";
        String newApkPath = "";
        if (ns != null) {
            diffInput = ns.getString("diffInput");
            oldApkPath = ns.getString("oldApkPath");
            newApkPath = ns.getString("newApkPath");
        }
        new AutoWindow().showWindow(diffInput, oldApkPath, newApkPath);
    }

    /**
     * @param args
     * @return
     */
    private static Namespace start(String[] args) {
        ArgumentParser parser =
                ArgumentParsers.newFor("DiffScript").build().defaultHelp(true).description("Calculate checksum of given files.");
        parser.addArgument("-df", "--diffInput")
                .required(false)
                .type(String.class)
                .dest("diffInput")
                .help("差分工具路径");
        parser.addArgument("-op", "--oldApkPath")
                .required(false)
                .type(String.class)
                .dest("oldApkPath")
                .help("旧的安装包路径");
        parser.addArgument("-np", "--newApkPath")
                .required(false)
                .type(String.class)
                .dest("newApkPath")
                .help("新的安装包路径");
//        parser.addArgument("-debug", "--assembleDebug")
//                .type(Boolean.class)
//                .nargs("?")
//                .setConst(true).dest("assembleDebug")
//                .help("开启自动Debug打包");
//        parser.addArgument("-alpha", "--assembleAlpha")
//                .type(Boolean.class).nargs("?")
//                .setConst(true)
//                .dest("assembleAlpha")
//                .help("开启自动Alpha打包");
//        parser.addArgument("-release", "--assembleRelease")
//                .type(Boolean.class)
//                .nargs("?")
//                .setConst(true)
//                .dest("assembleRelease")
//                .help("开启自动Release打包");
        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
            System.out.println(ns.toString());
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(0);
        }
        return ns;
    }
}

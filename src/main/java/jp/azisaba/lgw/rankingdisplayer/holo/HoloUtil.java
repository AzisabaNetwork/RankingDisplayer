package jp.azisaba.lgw.rankingdisplayer.holo;

import eu.decentsoftware.holograms.api.actions.ClickType;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.api.holograms.HologramLine;
import eu.decentsoftware.holograms.api.holograms.HologramPage;
import eu.decentsoftware.holograms.api.utils.collection.DList;
import org.bukkit.ChatColor;

import java.util.List;
import java.util.function.BiFunction;

public class HoloUtil {
    public static boolean applyPageRotation(Hologram hologram, List<String> pageNames, ChatColor selectedColor) {
        DList<HologramPage> pages = hologram.getPages();
        if (pages.isEmpty() || pages.size() == 1 || pages.size() != pageNames.size()) {
            return false;
        }
        for (int i = 0; i < pages.size(); i++) {
            HologramPage _p = pages.get(i);
            _p.addLine(new HologramLine(_p, _p.getNextLineLocation(), joinPageNames(pageNames, i, selectedColor)));
            // Go to prev page
            if (i == 0) {
                _p.addAction(ClickType.LEFT, HoloActions.getPageMoveAction(3));
            } else {
                _p.addAction(ClickType.LEFT, HoloActions.GO_PREV_PAGE);
            }

            // Go to next page
            if (i == pages.size() - 1) {
                _p.addAction(ClickType.RIGHT, HoloActions.getPageMoveAction(1));
            } else {
                _p.addAction(ClickType.RIGHT, HoloActions.GO_NEXT_PAGE);
            }
        }
        return true;
    }

    public static boolean applyPageRotationSingle(Hologram targetHolo, int pageIndex, List<String> pageNames, ChatColor selectedColor) {
        int targetHoloPageSize = targetHolo.getPages().size();
        if (targetHoloPageSize < pageIndex + 1) return false;
        HologramPage _p = targetHolo.getPage(pageIndex);
        int i = _p.getIndex();
        _p.addLine(new HologramLine(_p, _p.getNextLineLocation(), joinPageNames(pageNames, i, selectedColor)));
        // Go to prev page
        if (i == 0) {
            _p.addAction(ClickType.LEFT, HoloActions.getPageMoveAction(targetHoloPageSize));
        } else {
            _p.addAction(ClickType.LEFT, HoloActions.GO_PREV_PAGE);
        }

        // Go to next page
        if (i == targetHoloPageSize - 1) {
            _p.addAction(ClickType.RIGHT, HoloActions.getPageMoveAction(1));
        } else {
            _p.addAction(ClickType.RIGHT, HoloActions.GO_NEXT_PAGE);
        }
        return true;
    }

    private static String joinPageNames(List<String> pageNames, int index, ChatColor selectedColor) {
        if (pageNames.size() == 2) {
            return (index == 0 ? ChatColor.GREEN : "") + pageNames.get(0) + (index == 0 ? ChatColor.GRAY : ChatColor.GREEN) + pageNames.get(1);
        }
        StringBuilder _result = new StringBuilder(ChatColor.GRAY + "");
        for (int i = 0; i < pageNames.size(); i++) {
            if (i == index) {
                _result.append(selectedColor).append(pageNames.get(i)).append(ChatColor.GRAY);
            } else {
                _result.append(pageNames.get(i));
            }
            if (i != pageNames.size() - 1) _result.append(" ");
        }
        return _result.toString();
    }

    public static void addLines(HologramPage targetPage, List<String> lines) {
        for (String s : lines) {
            targetPage.addLine(new HologramLine(targetPage, targetPage.getNextLineLocation(), s));
        }
    }

    public static void addLinesByListFunc(HologramPage targetPage, List<String> targetList, BiFunction<Integer, String, String> getLineStrFunc) {
        for (int i = 0; i < targetList.size(); i++) {
            targetPage.addLine(new HologramLine(targetPage, targetPage.getNextLineLocation(), getLineStrFunc.apply(i, targetList.get(i))));
        }
    }
}

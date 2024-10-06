package jp.azisaba.lgw.rankingdisplayer.holo;

import eu.decentsoftware.holograms.api.actions.Action;

public class HoloActions {
    public static final Action GO_NEXT_PAGE = new Action("NEXT_PAGE");
    public static final Action GO_PREV_PAGE = new Action("PREV_PAGE");

    public static Action getAction(String str) {
        return new Action(str);
    }

    /**
     * @param targetPageNum minimum is 1
     * @return Action to move target page
     */
    public static Action getPageMoveAction(int targetPageNum) {
        return new Action("PAGE:" + targetPageNum);
    }
}

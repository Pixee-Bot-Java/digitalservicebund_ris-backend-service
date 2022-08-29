package de.bund.digitalservice.ris.domain.docx;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class NumberingList implements DocUnitDocx {

  private final List<NumberingListEntry> entries = new ArrayList<>();

  public NumberingList() {
    /*Create new instance of docUnitNumberList*/
  }

  public void addNumberingListEntry(NumberingListEntry entry) {
    entries.add(entry);
  }

  @Override
  public String toHtmlString() {
    StringBuilder sb = new StringBuilder();
    LinkedList<String> closeTags = new LinkedList<>();
    int[] cLvl = {-1};
    List<DocUnitNumberingListNumberFormat> currentNumberFormat = new ArrayList<>();

    entries.forEach(
        entry -> {
          int lvl;

          /*Get level of list entry*/
          try {
            lvl = Integer.parseInt(entry.iLvl());
          } catch (NumberFormatException e) {
            lvl = 0;
          }

          /*Open new List if change number format at lv 0*/
          if (shouldCreateNewList(cLvl[0], lvl, currentNumberFormat, entry.numberFormat())) {
            while (!closeTags.isEmpty()) {
              sb.append(closeTags.removeFirst());
            }
            sb.append(getOpenListTag(entry.numberFormat()));
            closeTags.addFirst(getCloseListTag(entry.numberFormat()));
            cLvl[0] = lvl;
          }

          /* Open list/sub-list Tag*/
          while (lvl > cLvl[0]) {
            sb.append(getOpenListTag(entry.numberFormat()));
            closeTags.addFirst(getCloseListTag(entry.numberFormat()));
            cLvl[0]++;
          }

          /* Close list/sub-list Tag*/
          while (lvl < cLvl[0]) {
            sb.append(closeTags.removeFirst());
            cLvl[0]--;
          }

          sb.append(entry.toHtmlString());
          if (!currentNumberFormat.isEmpty()) {
            currentNumberFormat.remove(0);
          }
          currentNumberFormat.add(entry.numberFormat());
          cLvl[0] = lvl;
        });

    /* Close all list/sub-list tag when last element*/
    while (!closeTags.isEmpty()) {
      sb.append(closeTags.removeFirst());
    }
    return sb.toString();
  }

  public enum DocUnitNumberingListNumberFormat {
    DECIMAL,
    BULLET,
    UPPER_ROMAN,
    LOWER_ROMAN,
    UPPER_LETTER,
    LOWER_LETTER,
  }

  private String getOpenListTag(DocUnitNumberingListNumberFormat listNumberFormat) {
    switch (listNumberFormat) {
      case DECIMAL -> {
        return "<ol class=\"decimal\">";
      }
      case LOWER_ROMAN -> {
        return "<ol class=\"lower-roman\">";
      }
      case UPPER_ROMAN -> {
        return "<ol class=\"upper-roman\">";
      }
      case LOWER_LETTER -> {
        return "<ol class=\"lower-letter\">";
      }
      case UPPER_LETTER -> {
        return "<ol class=\"upper-letter\">";
      }
      default -> {
        return "<ul>";
      }
    }
  }

  private String getCloseListTag(DocUnitNumberingListNumberFormat listNumberFormat) {
    return listNumberFormat == DocUnitNumberingListNumberFormat.BULLET ? "</ul>" : "</ol>";
  }

  private boolean shouldCreateNewList(
      int cLvl,
      int nLvl,
      List<DocUnitNumberingListNumberFormat> cNumberFormat,
      DocUnitNumberingListNumberFormat nNumberFormat) {
    if (cNumberFormat.isEmpty() || nNumberFormat.equals(cNumberFormat.get(0))) return false;
    if (nLvl != 0) return false;
    if (cLvl == 0) return true;
    return nNumberFormat.equals(DocUnitNumberingListNumberFormat.BULLET);
  }
}

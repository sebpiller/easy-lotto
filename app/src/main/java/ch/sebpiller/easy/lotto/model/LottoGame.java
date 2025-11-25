package ch.sebpiller.easy.lotto.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LottoGame {
    enum GameStep {
        QUINE(1), DQUINE(2), CARTON(3);

        private final int rowsToWin;

        GameStep(int i) {
            this.rowsToWin = i;
        }

        public boolean canBeWinWithRows(int rowFull) {
            return rowFull >= rowsToWin;
        }
    }

    private GameStep step = GameStep.QUINE;
    private final Set<Integer> numbers = new HashSet<>(90);
    private final List<LottoGrid> userGrids = new ArrayList<>();

    public void removeAllGrids() {
        userGrids.clear();
    }

    public void addGrid(LottoGrid grid) {
        userGrids.add(grid);
    }

    public void reset() {
        step = GameStep.QUINE;
        numbers.clear();
    }

    public void nextPart() {
        switch (step) {
            case QUINE:
                step = GameStep.DQUINE;
                break;
            case DQUINE:
                step = GameStep.CARTON;
                break;
            case CARTON:
                step = GameStep.QUINE;
                break;
        }
    }

    public void pushNumber(int number) {
        if (number < 1 || number > 90) {
            throw new IllegalArgumentException("number must be between 1 and 90");
        }

        if (!numbers.add(number)) {
            System.err.println("the number " + number + " has already been given !");
        }
    }

    public boolean checkWin() {
        for (var grid : userGrids) {
            var rowFull = 0;

            for (var row = 0; row < 3; row++) {
                var foundNOnRow = 0;

                for (var col = 0; col < 9; col++) {
                    var n = grid.findAt(row, col);

                    if (n != null && numbers.contains(n.getValue())) {
                        foundNOnRow++;
                    }
                }

                if (foundNOnRow == 5) {
                    rowFull++;
                }
            }

            if (step.canBeWinWithRows(rowFull)) {
                return true;
            }
        }

        return false;
    }
}

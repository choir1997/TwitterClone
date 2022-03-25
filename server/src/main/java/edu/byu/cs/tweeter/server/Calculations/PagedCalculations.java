package edu.byu.cs.tweeter.server.Calculations;

import java.util.List;

import edu.byu.cs.tweeter.model.domain.Status;

public class PagedCalculations<T, W> {
    public int getItemsStartingIndex(T lastItem, List<W> items) {
        int index = 0;

        if(lastItem != null) {
            // This is a paged request for something after the first page. Find the first item
            // we should return
            for (int i = 0; i < items.size(); i++) {
                if(lastItem.equals(items.get(i))) {
                    // We found the index of the last item returned last time. Increment to get
                    // to the first one we should return
                    index = i + 1;
                    break;
                }
            }
        }

        return index;
    }
}

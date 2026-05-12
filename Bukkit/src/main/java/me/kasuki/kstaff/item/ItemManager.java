package me.kasuki.kstaff.item;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import me.kasuki.kstaff.KStaffPlugin;
import me.kasuki.kstaff.item.impl.RandomTPItem;

public class ItemManager {
    private final KStaffPlugin instance;
    private List<AbstractItem> items;

    public ItemManager(KStaffPlugin instance) {
        this.instance = instance;
        this.items = new ArrayList<>();
    }

    public AbstractItem getFromId(String id){
        return items.stream().filter(item -> item.getId().equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);
    }

    public void init(){
        Stream.of(
                new RandomTPItem(this.instance)
        ).forEach(item -> this.items.add((AbstractItem) item));
    }
}

package code;

public class Value {
    public Integer stores;

    public Value(Integer stores) {
        this.stores = stores;
    }

    @Override
    public String toString(){
        return Integer.toString(stores);
    }

    public void increment(){
        stores++;
    }

    public void decrement(){
        stores--;
    }
}

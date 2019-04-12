package code;

public class Value {
    public Integer stores;
    public Integer rd;

    public Value(Integer stores, Integer rd) {
        this.stores = stores;
        this.rd = rd;
    }

    @Override
    public String toString(){
        return stores + " " + rd;
    }

    public void increment(){
        stores++;
    }

    public void decrement(){
        stores--;
    }
}

/**
 * Created by Geek on 5/1/2018.
 */

class Books_Warehouse {

    private int Shelf;
    private String ISBN;
    private int quantity;

    public int getShelf() {
        return Shelf;
    }

    public void setShelf(int shelf) {
        Shelf = shelf;
    }

    public String getISBN() {
        return ISBN;
    }

    public void setISBN(String ISBN) {
        this.ISBN = ISBN;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}

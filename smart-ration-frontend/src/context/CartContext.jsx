import { createContext, useContext, useMemo, useState } from "react";

const CartContext = createContext(null);

const storedCart = localStorage.getItem("smartRationCart");

export function CartProvider({ children }) {
  const [cartItems, setCartItems] = useState(storedCart ? JSON.parse(storedCart) : []);

  const persistCart = (items) => {
    setCartItems(items);
    localStorage.setItem("smartRationCart", JSON.stringify(items));
  };

  const addToCart = (product) => {
    const existingItem = cartItems.find((item) => item.id === product.id);

    if (existingItem) {
      persistCart(
        cartItems.map((item) =>
          item.id === product.id ? { ...item, selectedQuantity: item.selectedQuantity + 1 } : item
        )
      );
      return;
    }

    persistCart([...cartItems, { ...product, selectedQuantity: 1 }]);
  };

  const updateQuantity = (productId, quantity) => {
    if (quantity <= 0) {
      removeFromCart(productId);
      return;
    }

    persistCart(
      cartItems.map((item) =>
        item.id === productId ? { ...item, selectedQuantity: quantity } : item
      )
    );
  };

  const removeFromCart = (productId) => {
    persistCart(cartItems.filter((item) => item.id !== productId));
  };

  const clearCart = () => {
    persistCart([]);
  };

  const totalItems = cartItems.reduce((total, item) => total + item.selectedQuantity, 0);

  const value = useMemo(
    () => ({
      cartItems,
      addToCart,
      updateQuantity,
      removeFromCart,
      clearCart,
      totalItems
    }),
    [cartItems, totalItems]
  );

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}

export function useCart() {
  return useContext(CartContext);
}

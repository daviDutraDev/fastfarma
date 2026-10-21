import { createContext, useContext } from "react";

export const ConfirmacaoContext = createContext(null);

export function useConfirmacao() {
  return useContext(ConfirmacaoContext);
}

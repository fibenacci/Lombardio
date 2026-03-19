import { readRuntimeValue } from "../../config/runtime-config";

const DEFAULT_WS_URL = readRuntimeValue(
  "CENTRIFUGO_WS_URL",
  import.meta.env.VITE_CENTRIFUGO_WS_URL ?? "ws://localhost:8000/connection/websocket"
);

export function connectToAuctionRealtime(session, { onState, onPublication }) {
  const socket = new WebSocket(session?.wsUrl ?? DEFAULT_WS_URL);
  let connected = false;

  socket.addEventListener("open", () => {
    onState?.("connecting");
    socket.send(JSON.stringify({ id: 1, connect: { token: session.connectionToken } }));
  });

  socket.addEventListener("message", (event) => {
    const message = JSON.parse(event.data);
    if (message.connect) {
      connected = true;
      onState?.("connected");
      socket.send(JSON.stringify({ id: 2, subscribe: { channel: session.channel, token: session.subscriptionToken } }));
      return;
    }
    if (message.push?.pub?.data) {
      onPublication?.(message.push.pub.data);
    }
  });

  socket.addEventListener("close", () => {
    onState?.(connected ? "closed" : "failed");
  });

  socket.addEventListener("error", () => {
    onState?.("error");
  });

  return () => socket.close();
}

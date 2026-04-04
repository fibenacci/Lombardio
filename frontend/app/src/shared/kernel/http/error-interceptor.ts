export type ErrorHandler = (error: any) => void | Promise<void>;

class ErrorInterceptorRegistry {
  private handlers: Map<number, ErrorHandler[]> = new Map();
  private globalHandlers: ErrorHandler[] = [];

  /**
   * Register a handler for a specific HTTP status code.
   */
  on(status: number, handler: ErrorHandler): () => void {
    const statusHandlers = this.handlers.get(status) || [];
    this.handlers.set(status, [...statusHandlers, handler]);
    
    return () => {
      const current = this.handlers.get(status) || [];
      this.handlers.set(status, current.filter(h => h !== handler));
    };
  }

  /**
   * Register a global handler for all failed requests.
   */
  onAny(handler: ErrorHandler): () => void {
    this.globalHandlers.push(handler);
    return () => {
      this.globalHandlers = this.globalHandlers.filter(h => h !== handler);
    };
  }

  /**
   * Dispatch an error to all matching handlers.
   */
  async dispatch(error: any): Promise<void> {
    const status = error.status;
    const matchingHandlers = this.handlers.get(status) || [];
    
    const allHandlers = [...matchingHandlers, ...this.globalHandlers];
    
    for (const handler of allHandlers) {
      try {
        await handler(error);
      } catch (e) {
        console.error("[ErrorInterceptor] Handler failed", e);
      }
    }
  }
}

export const errorInterceptor = new ErrorInterceptorRegistry();

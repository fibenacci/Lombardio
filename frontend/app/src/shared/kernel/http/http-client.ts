export interface HttpClient {
  get<T>(url: string, token?: string): Promise<T>;
  post<T>(url: string, payload?: unknown, token?: string): Promise<T>;
  put<T>(url: string, payload?: unknown, token?: string): Promise<T>;
  delete<T>(url: string, token?: string): Promise<T>;
}

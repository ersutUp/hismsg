/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

declare module 'js-cookie' {
  interface CookieAttributes {
    expires?: number | Date
    path?: string
    domain?: string
    secure?: boolean
    sameSite?: 'strict' | 'lax' | 'none'
  }

  interface CookiesStatic {
    get(): { [key: string]: string }
    get(name: string): string | undefined
    set(name: string, value: string | object, options?: CookieAttributes): string | undefined
    remove(name: string, options?: CookieAttributes): void
  }

  const Cookies: CookiesStatic
  export default Cookies
}
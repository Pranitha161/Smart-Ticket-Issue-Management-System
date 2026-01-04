import { Injectable } from '@angular/core';
import { HttpContext } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { Category, CategoryDto } from './category';
import { AdminUser, User } from './admin-user';
import { SlaRule } from './sla-rule';
import { SlaRuleModel } from '../../shared/models/sla-rule.model';
import { BYPASS_LOGIC } from '../constants/tokens';

@Injectable({ providedIn: 'root' })
export class LookupService {
  private categories = new Map<string, CategoryDto>();
  private users = new Map<string, User>();
  private slas = new Map<string, SlaRuleModel>();

  constructor(
    private categoryService: Category,
    private userService: AdminUser,
    private slaService: SlaRule
  ) { }

  async init(): Promise<void> {
    try {
      const bypassConfig = {
        context: new HttpContext().set(BYPASS_LOGIC, true)
      };
      const [cats, users, slas] = await Promise.all([
        firstValueFrom(this.categoryService.getAllCategories(bypassConfig)),
        firstValueFrom(this.userService.getAllUsers(bypassConfig)),
        firstValueFrom(this.slaService.getAllRules(bypassConfig))
      ]);

      cats?.forEach(c => c.id && this.categories.set(c.id, c));
      users?.forEach(u => this.users.set(u.id, u));
      slas?.forEach(s => s.id && this.slas.set(s.id, s));
      console.log(this.categories);
      console.log('Lookup data loaded:', this.users.size, 'users found');
    } catch (err) {
      console.error('Lookup init failed:', err);
    }
  }

  // getCategoryName(id?: string): string {
  //   console.log(this.categories);
  //   return id ? (this.categories.get(id)?.name || id) : '';
  // }
  getCategoryName(id?: string): string {
  if (!id) return 'Uncategorized';
  
  // Clean the ID and check the map
  const cleanId = id.toString().trim();
  const category = this.categories.get(cleanId);
  
  if (category) {
    return category.name;
  }

  // Debug: If it still returns the ID, we can see what's failing in console
  // console.warn(`Lookup failed for ID: ${cleanId}. Map size: ${this.categories.size}`);
  return id; 
}
  getUserName(id?: string): string {
    return id ? (this.users.get(id)?.username || id) : '';
  }

  getUserEmail(id?: string): string {
    return id ? (this.users.get(id)?.email || id) : '';
  }

  getSlaName(id?: string): string {
    return id ? (this.slas.get(id)?.priority || id) : '';
  }

  getCategoryList(): CategoryDto[] {
    return Array.from(this.categories.values());
  }

  getUserList(): User[] {
    return Array.from(this.users.values());
  }

  getSlaList(): SlaRuleModel[] {
    return Array.from(this.slas.values());
  }



  async refreshCategories(): Promise<void> {
    const context = { context: new HttpContext().set(BYPASS_LOGIC, true) };
    const cats = await firstValueFrom(this.categoryService.getAllCategories(context));
    this.categories.clear();
    cats?.forEach(c => c.id && this.categories.set(c.id, c));
  }

  async refreshUsers(): Promise<void> {
    const context = { context: new HttpContext().set(BYPASS_LOGIC, true) };
    const users = await firstValueFrom(this.userService.getAllUsers(context));
    this.users.clear();
    users?.forEach(u => this.users.set(u.id, u));
  }


  clear(): void {
    this.categories.clear();
    this.users.clear();
    this.slas.clear();
    console.log('🧹 Lookup data wiped from memory');
  }
  async refreshSlas(): Promise<void> {
    const context = { context: new HttpContext().set(BYPASS_LOGIC, true) };
    const slas = await firstValueFrom(this.slaService.getAllRules(context));
    this.slas.clear();
    slas?.forEach(s => s.id && this.slas.set(s.id, s));
  }
}
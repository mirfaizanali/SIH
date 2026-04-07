import { Pipe, PipeTransform } from '@angular/core';
import { formatCurrency } from '@angular/common';

@Pipe({ name: 'currencyInr', standalone: true, pure: true })
export class CurrencyInrPipe implements PipeTransform {
  transform(value: number | null | undefined): string {
    if (value == null) return '';
    return formatCurrency(value, 'en-IN', '₹', 'INR', '1.0-0');
  }
}

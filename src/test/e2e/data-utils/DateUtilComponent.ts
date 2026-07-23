export default class DateUtilComponent {

    static addWeekdays(startDate: Date, days: number): Date {
        let count = 0;
        let currentDate = new Date(startDate);
        const increment = days > 0 ? 1 : -1;

        while (Math.abs(count) < Math.abs(days)) {
            currentDate.setDate(currentDate.getDate() + increment);
            const dayOfWeek = currentDate.getDay();
            if (dayOfWeek !== 0 && dayOfWeek !== 6) {
                count += increment;
            }
        }
        return currentDate;
    }


    static formatTodaysDate(date: Date): string {

        let day = date.getDate();
        let month = date.toLocaleString('default', {month: 'short'});
        let year = date.getFullYear();

        return `${day} ${month} ${year}`;
    }

    static addDaysAndMonths(days: number, month?: number): string {
        const today = new Date();
        today.setDate(today.getDate() + days);
        if(month) today.setMonth(today.getMonth() + month);
        return this.formatTodaysDate(today);
    }

  static getCurrentDateParts(): { dd: string; mm: string; yyyy: string } {
    const now = new Date();

    const dd = String(now.getDate()).padStart(2, '0');
    const mm = String(now.getMonth() + 1).padStart(2, '0'); // Months are zero-based
    const yyyy = String(now.getFullYear());

    return { dd, mm, yyyy };
  }

  /**
   * Returns today's date as a string in "YYYY-MM-DD" format.
   *
   * @returns Current date string in ISO format (date only) as a promise resolving to a string.
   */
  static getCurrentDate(): string {
    return new Date().toISOString().split('T')[0];
  }

  /**
   * Returns a timestamp.  It's UTC.
   * Formated as: "2023-10-06T12:34:56.789Z".  Postgres stores as "2023-10-06T12:34:56.789000" (localtime).
   *
   * @returns Current datetime string in ISO format as a promise resolving to a string.
   */
  static async getCurrentTimestamp(): Promise<string> {
    return new Date().toISOString();
  }

  /**
   * Returns today's date formatted as "d MMM yyyy" (e.g. "6 Aug 2025").
   *
   * @returns Formatted current date string.
   */
  static getTodayFormattedDate(): string {
    const today = new Date();
    return today
      .toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' })
      .replace(/\b([A-Za-z]{4,})\b/g, m => {return m.slice(0, 3);});
  };

  /**
   * Returns today's date formatted as an array of strings [year, month, day].
   *
   * @returns An array containing the year, month, and day as strings.
   */
  static getCurrentDateSliced(): string[] {
    const today = this.getCurrentDate();
    return today.split('-');
  };

  /**
   * Returns the current UK date and time formatted using Europe/London timezone.
   * DST is handled automatically by the runtime (BST in summer, GMT in winter).
   *
   * @returns Formatted UK date and time string including timezone abbreviation.
   */
  static getUtcDateTimeFormatted(): string {
    const now = new Date();
    return now.toLocaleString('en-GB', {
      day: 'numeric',
      month: 'long',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      hour12: false,
      timeZone: 'Europe/London',
    })
      .replace(/\b( am| pm)\b/i, '')
      .replace(/\bSept\b/, 'Sep')
      .replace(/\bat \b/, '');
  }

  /**
   * Returns today's date formatted as "d Month yyyy" (e.g. "06 August 2025").
   *
   * @returns Formatted current date string.
   */
  static getTodayFullFormattedDate(): string {
    return this.formatToDayMonthYear(new Date());
  };

  /**
   * Converts a date string (ISO format) into a formatted date string
   * in the format "dd Month yyyy" (e.g. "06 August 2025").
   *
   * @returns A promise that resolves with the formatted date string.
   * @param date
   */
  static formatToDayMonthYear(date: Date): string {
    return new Intl.DateTimeFormat('en-GB', {
      day: 'numeric',
      month: 'long',
      year: 'numeric'
    }).format(date);
  }

  /**
   * Converts a date string (ISO format) into a formatted date string
   * in the short month format "dd Month yyyy" (e.g. "06 Aug 2025").
   *
   * @param dateStr - A valid ISO date string (e.g. "2025-08-06").
   * @returns A promise that resolves with the formatted date string.
   */
  static formatToDayMonthYearShort(dateStr: string): string {
    const date = new Date(dateStr);
    return new Intl.DateTimeFormat('en-GB', {
      day: 'numeric',
      month: 'short',
      year: 'numeric'
    }).format(date);
  }

  static formatDateWithLeadingZeros(date:Date){

    let day = date.getDate();
    let day1:string=""
    if (day<10){
      day1="0".concat(String(day))
    } else {
      day1= String(day)
    }
    let month = date.toLocaleString('default', {month: 'short'});
    let year = date.getFullYear();

    return `${day1} ${month} ${year}`;
  }

}

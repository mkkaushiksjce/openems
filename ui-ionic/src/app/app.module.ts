import { BrowserModule } from '@angular/platform-browser';
import { NgModule, ErrorHandler } from '@angular/core';
import { IonicApp, IonicModule, IonicErrorHandler } from 'ionic-angular';
import { MyApp } from './app.component';

import { OverviewPage } from '../pages/overview/overview';


import { StatusBar } from '@ionic-native/status-bar';
import { SplashScreen } from '@ionic-native/splash-screen';
import { DoverlayPage, TabContentPage } from '../pages/device/doverlay/doverlay';
import { DoverlayPageModule } from '../pages/device/doverlay/doverlay.module';

import { GsettingsPage } from '../pages/gsettings/gsettings';

import { CalendarModule, CalendarComponentOptions } from "ion2-calendar";
import { ChartsModule } from 'ng2-charts';
import { OwpopoverPage } from '../pages/owpopover/owpopover';
import { DpopoverPage } from '../pages/device/dpopover/dpopover';
import { DsettingsPage } from '../pages/device/dsettings/dsettings';
import { AboutPage } from '../pages/about/about';
import { TablePage } from '../pages/device/table/table';
import { HistoryPage } from '../pages/device/history/history';
import { TimemenuePage } from '../pages/device/history/timemenue/timemenue';
import { MonitorPage } from '../pages/device/monitor/monitor';



@NgModule({
  declarations: [
    MyApp,
    OverviewPage,
    TabContentPage,
    AboutPage,
    DpopoverPage,
    OwpopoverPage,
    GsettingsPage,
    DsettingsPage,
    DoverlayPage,
    TablePage,
    HistoryPage,
    TimemenuePage,
    MonitorPage
  ],
  imports: [
    BrowserModule,
    IonicModule.forRoot(MyApp, { tabsPlacement: 'top' }),
    CalendarModule,
    ChartsModule
  ],
  bootstrap: [IonicApp],
  entryComponents: [
    MyApp,
    OverviewPage,
    DoverlayPage,
    AboutPage,
    DpopoverPage,
    OwpopoverPage,
    GsettingsPage,
    DsettingsPage,
    TablePage,
    HistoryPage,
    TimemenuePage,
    MonitorPage
  ],
  providers: [
    StatusBar,
    SplashScreen,
    { provide: ErrorHandler, useClass: IonicErrorHandler }
  ]
})
export class AppModule { }

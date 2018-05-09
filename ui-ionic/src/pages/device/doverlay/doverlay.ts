import { Component } from '@angular/core';
import { IonicPage, NavController, NavParams } from 'ionic-angular';
import { PopoverController } from 'ionic-angular';
import { DpopoverPage } from '../dpopover/dpopover';
import { OwpopoverPage } from '../../owpopover/owpopover';
import { DsettingsPage } from '../dsettings/dsettings';
import { MonitorPage } from '../monitor/monitor';
import { TablePage } from '../table/table';
import { HistoryPage } from '../history/history';
import { DefaultMessages } from '../../shared/service/defaultmessages';
import { DefaultTypes } from '../../shared/service/defaulttypes';
import { Websocket } from '../../websocket/websocket';
import { CurrentDataAndSummary } from '../../deviceconfig/currentdata';
import { webSocket } from 'rxjs/observable/dom/webSocket';
import { HostListener } from '@angular/core';
import { MonitablePage } from '../../monitable/monitable';




@Component({
  template: `
  <ion-header>
    <ion-navbar>
      <ion-title>Tabsx</ion-title>
    </ion-navbar>
  </ion-header>
  <ion-content></ion-content>
  `
})
export class TabContentPage {
  constructor(public popoverCtrl: PopoverController, public websocket: Websocket, public defaultmessages: DefaultMessages) { }

  presentPopover(myEvent) {
    let popover = this.popoverCtrl.create(DpopoverPage);
    popover.present({
      ev: myEvent
    });
  }


}



@IonicPage()
@Component({
  selector: 'page-doverlay',
  templateUrl: 'doverlay.html',

})

export class DoverlayPage {

  [x: string]: any;
  tab1 = MonitorPage;
  tab2 = TablePage;
  tab3 = HistoryPage;
  tab4 = MonitablePage;
  public edgeId: number;
  isMobile: boolean = true;



  constructor(public navCtrl: NavController, public popoverCtrl: PopoverController, public navParams: NavParams, public websocket: Websocket) {
    console.log("doverlay: " + navParams.get('edgeId'));
    this.edgeId = navParams.get('edgeId');


  }
  @HostListener('window:resize', ['$event'])

  ionViewDidLoad() {
    console.log('ionViewDidLoad DoverlayPage');
    this.websocket.getConfig(this.edgeId);
    


    this.innerWidth = window.innerWidth;
    console.log(innerWidth)
    if (this.innerWidth >= 1280) {
      this.isMobile = false;
    }

    else {
      this.isMobile = true;
    }
    console.log(this.isMobile);
    // this.websocket.currentdatasub();
  }
  ionViewDidLeave(){
    console.log("WIR SIND RAUUUUUUS");
  }
  itemTapped(event) {
    this.navCtrl.push(DsettingsPage);
  }
  presentPopover(myEvent) {
    let popover = this.popoverCtrl.create(DpopoverPage);
    popover.present({
      ev: myEvent
    });
  }



  onresize(event) {
    this.innerWidth = window.innerWidth;
  }
}



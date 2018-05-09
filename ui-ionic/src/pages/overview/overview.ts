import { Component } from '@angular/core';
import { NavController, NavParams } from 'ionic-angular'
import { PopoverController } from 'ionic-angular';
import { DoverlayPage } from '../device/doverlay/doverlay';
import { DpopoverPage } from '../device/dpopover/dpopover';
import { OwpopoverPage } from '../owpopover/owpopover';
import { WebSocketSubject } from 'rxjs/observable/dom/WebSocketSubject';
import { Websocket } from '../websocket/websocket';
import { LoginPage } from '../login/login';
import { Service } from '../shared/service/service';


@Component({
  selector: 'page-overview',
  templateUrl: 'overview.html'
})
export class OverviewPage {
  // edgeId = this.websocket.edges

  private socket: WebSocketSubject<any>;



  showSearchBar: boolean = false;

  [x: string]: any;
  constructor(public navCtrl: NavController,
    public popoverCtrl: PopoverController,
    public websocket: Websocket,
     private service: Service,
   //  public loginpage: LoginPage
    ) { }

  // ionViewDidLoad() {
  //   if (this.loginpage.savecookie == false) {
  //     this.service.removeToken();
  //   }
  // }


  itemTapped(edgeId) {
    console.log("itemTapped", edgeId);
    this.navCtrl.push(DoverlayPage, {
      // test: 5
      edgeId: edgeId
      // edgeId: this.websocket.edges
    });
  }

  presentPopover(myEvent) {
    let popover = this.popoverCtrl.create(OwpopoverPage);
    popover.present({
      ev: myEvent
    });
  }

  logout() {
    this.websocket.logout();
    this.navCtrl.push(LoginPage);
  }


}
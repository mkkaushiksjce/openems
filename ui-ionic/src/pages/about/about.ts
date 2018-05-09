import { Component } from '@angular/core';
import { IonicPage, NavController, NavParams } from 'ionic-angular';
import { OverviewPage } from '../overview/overview';
import { DoverlayPage } from '../device/doverlay/doverlay';
import { Observable, Subject } from 'rxjs/Rx';

import { WebSocketSubject } from 'rxjs/observable/dom/WebSocketSubject';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';
import { DefaultTypes } from '../shared/service/defaulttypes';
import { UUID } from 'angular2-uuid';
import { format } from 'date-fns';
import { Websocket } from '../websocket/websocket';
import { variable } from '@angular/compiler/src/output/output_ast';
//import { environment as env } from 

@Component({
  selector: 'page-about',
  templateUrl: 'about.html'
})
export class AboutPage {
  private socket: WebSocketSubject<any>;
  public status: DefaultTypes.ConnectionStatus = "connecting";
  public isWebsocketConnected: BehaviorSubject<boolean> = new BehaviorSubject(false);
  private replyStreams: { [deviceName: string]: { [messageId: string]: Subject<any> } } = {};

  private platzhalter: { [messageId: string]: Subject<any> } = {};
  // {
  //   "ID1": new Subject(),
  //   "ID2": new Subject()
  // }
  private event = new Subject<String>();

  constructor(public navCtrl: NavController, public navParams: NavParams, public websocket: Websocket) {
  }

  connect() {
    this.websocket.connect();
  }



  getConfig(edgeId: number) {
    console.log("getConfig()");
    /*
     * UI send
     */
    let message = {
      messageId: {
        ui: UUID.UUID()
      },
      edgeId: 0,
      config: {
        language: "de",
        mode: "query"
      }
    }
    let messageId = message.messageId.ui;
    this.platzhalter[messageId] = new Subject();
    console.log("Alle Platzhalter: ", this.platzhalter);

    this.platzhalter[messageId].subscribe(message => {
      console.log("Platzhalter: ", message);
      this.platzhalter[messageId].unsubscribe();
      delete this.platzhalter[messageId];
    })


    console.log("SEND: ", message);
    this.socket.socket.send(JSON.stringify(message));
  }

  queryhistoric(edgeId: number, fromDate: Date, toDate: Date, timezone: number, channels: DefaultTypes.ChannelAddresses): void {
    console.log("queryhistory");
    let message = {
      messageId: {
        ui: UUID.UUID()
      },
      edgeId: 0,
      historicData: {
        mode: "query",
        fromDate: format(fromDate, 'YYYY-MM-DD'),
        toDate: format(toDate, 'YYYY-MM-DD'),
        timezone: timezone,
        channels: channels
      }
    }
    let messageId = message.messageId.ui;
    this.platzhalter[messageId] = new Subject();
    console.log("Alle Platzhalter: ", this.platzhalter);

    this.platzhalter[messageId].subscribe(message => {
      console.log("Platzhalter: ", message);
      this.platzhalter[messageId].unsubscribe();
      delete this.platzhalter[messageId];
    })
    console.log("SEND: ", message);
    this.socket.socket.send(JSON.stringify(message));
  }
}

// test1() {
//   console.log(this.websocket.test);
// this.platzhalter.next("Neue Nachricht");


// subscribe() {
//   let message = {
//     currentdata: {
//       mode: "subscribe",
//       channels: {
//         _bridge0: ["State"],
//         _bridge1: ["State"],
//         _controller0: ["State"],
//         _controller1: ["State"],
//         _controller3: ["State"],
//       }
//     }
//   }
//   console.log("SEND: ", message);
//   this.socket.socket.send(JSON.stringify(message));
// }

// subscribe(edgeId: 0, thingId: string, channelId: string, value: any): void {
//   let message = {
//     messageId: {
//       ui: UUID.UUID()
//   },
//   edgeId: 0,
//   config: {
//       mode: "update",
//       thing: thingId,
//       channel: channelId,
//       value: value
//   }
//   }
//   console.log("SEND: ", message);
//   this.socket.socket.send(JSON.stringify(message));

// }

//  currentdatasub(edgeId: number, channels: DefaultTypes.ChannelAddresses): void {
//    let message = {
//      messageId: {
//        ui: UUID.UUID()
//      },
//      edgeId: 0,
//      currentData: {
//        mode: "subscribe",
//        channels: {
//          _bridge0: ["State"],
//          _bridge1: ["State"],
//          _controller0: ["State"],
//         _controller1: ["State"],
//          _controller2: ["State"],
//          _controller3: ["State"],
//          _controller4: ["State"],
//          _device0: ["State"],
//          _device1: ["State"],
//          _persistence0: ["State"],
//          _scheduler0: ["State"],
//          ess0: ["State"],
//          meter0: ["State"],
//          meter1: ["State"],
//          system0: ["State"]
//        }
//      }
//    }
//    console.log("SEND: ", message);
//    this.socket.socket.send(JSON.stringify(message));
//  }

  // queryhistoric(edgeId: number, fromDate: Date, toDate: Date, timezone: number, channels: DefaultTypes.ChannelAddresses): void {
  //   let message = {
  //     messageId: {
  //       ui: UUID.UUID()
  //     },
  //     edgeId: 0,
  //     historicData: {
  //       mode: "query",
  //       fromDate: format(fromDate, 'YYYY-MM-DD'),
  //       toDate: format(toDate, 'YYYY-MM-DD'),
  //       timezone: timezone,
  //       channels: channels
  //     }
  //   }
  //   console.log("SEND: ", message);
  //   this.socket.socket.send(JSON.stringify(message));
  // }

  // logout(): void {
  // //   this.event.next("Hallo Welt");

  //     let message = {
  //       authenticate: {
  //    //     mode: "logout"
  //    //   }
  //    // }
  //    // console.log("SEND: ", message);
  //     this.socket.socket.send(JSON.stringify(message));
  //  }






import {Component, OnInit} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import {PollSource} from '../pollsource.model';
import {ActivatedRoute, Router} from '@angular/router';
import 'rxjs/add/operator/switchMap'
import {PollsourceService} from '../pollsource.service';

@Component({
  selector: 'app-pollsource-list',
  templateUrl: './pollsource-list.component.html',
  styleUrls: ['./pollsource-list.component.css']
})
export class PollsourceListComponent implements OnInit {

  private pollSources$: Observable<Array<PollSource>>;

  clientId: string;

  constructor(private router: Router,
              private route: ActivatedRoute,
              private service: PollsourceService) {
  }

  ngOnInit() {
    this.pollSources$ = this.route.paramMap.switchMap(params => {
      this.clientId = params.get('clientId');
      return this.service.getAll(this.clientId);
    });
  }

  back() {
    this.router.navigate(['/client']);
  }

}
